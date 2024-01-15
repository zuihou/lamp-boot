package top.tangyh.lamp.base.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.tangyh.basic.context.ContextUtil;
import top.tangyh.basic.exception.ForbiddenException;
import top.tangyh.lamp.common.properties.IgnoreProperties;
import top.tangyh.lamp.oauth.biz.ResourceBiz;


/**
 * 一定要在 TokenContextFilter 之后执行
 *
 * @author zuihou
 * @date 2021/12/7 22:10
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements AsyncHandlerInterceptor {
    private final static String UN_APPLICATION_AUTHORIZED = "对不起，您无该应用的权限!";
    private final static String UN_RESOURCE_AUTHORIZED = "对不起，您无该URI资源的权限!";
    private final IgnoreProperties ignoreProperties;
    private final ResourceBiz oauthResourceBiz;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            log.debug("not exec!!! url={}", request.getRequestURL());
            return true;
        }
        if (!ignoreProperties.getAuthEnabled()) {
            log.debug("已全局禁用URI权限");
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        // 1. 是否忽略验证
        // 判断接口是否需要忽略token验证
        if (isIgnoreUriAuth(request)) {
            log.debug("当前接口：{}, 请求方式={}, 忽略权限验证", path, method);
            return true;
        }

        Long applicationId = ContextUtil.getApplicationId();
        Long employeeId = ContextUtil.getEmployeeId();

        // 3. 普通用户 需要校验 uri + method 的权限, 租户管理员 拥有分配给该企业的所有 资源权限
        Boolean hasApi = oauthResourceBiz.checkUri(path, method);
        if (!hasApi) {
            log.warn("4. uri={}, applicationId={}, employeeId={} ", path, applicationId, employeeId);
            throw ForbiddenException.wrap(UN_RESOURCE_AUTHORIZED);
        }
        log.info("thread id ={}, name={}", Thread.currentThread().getId(), Thread.currentThread().getName());
        return true;
    }

    /**
     * 忽略应用级token
     *
     * @return
     */
    protected boolean isIgnoreUriAuth(HttpServletRequest request) {
        return ignoreProperties.isIgnoreUriAuth(request.getMethod(), request.getRequestURI());
    }
}
