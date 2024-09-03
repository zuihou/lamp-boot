package top.tangyh.lamp.base.interceptor;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import top.tangyh.basic.context.ContextConstants;
import top.tangyh.basic.context.ContextUtil;
import top.tangyh.lamp.common.properties.IgnoreProperties;
import top.tangyh.lamp.oauth.biz.ResourceBiz;
import top.tangyh.lamp.system.vo.result.application.ResourceApiVO;

import java.util.Map;
import java.util.Set;

import static top.tangyh.basic.context.ContextConstants.JWT_KEY_COMPANY_ID;
import static top.tangyh.basic.context.ContextConstants.JWT_KEY_DEPT_ID;
import static top.tangyh.basic.context.ContextConstants.JWT_KEY_EMPLOYEE_ID;
import static top.tangyh.basic.context.ContextConstants.JWT_KEY_TOP_COMPANY_ID;

/**
 * 配合sa-token，实现 登录校验、uri校验
 *
 * @author zuihou
 * @since 2021/12/7 22:10
 */
@Slf4j
public class AuthenticationSaInterceptor extends SaInterceptor {
    private final ResourceBiz resourceBiz;
    private final IgnoreProperties ignoreProperties;

    public AuthenticationSaInterceptor(IgnoreProperties ignoreProperties, ResourceBiz resourceBiz) {
        this.resourceBiz = resourceBiz;
        this.ignoreProperties = ignoreProperties;
        this.auth = handler -> {
            Map<String, Set<String>> anyUser = ignoreProperties.buildAnyUser();
            // 验证token 排除掉需要租户ID，但不需要登录
            SaRouter
                    .match("/**")    // 拦截的 path 列表，可以写多个 */
                    .notMatch(r -> {
                        String path = SaHolder.getRequest().getRequestPath();
                        String method = SaHolder.getRequest().getMethod();
                        for (Map.Entry<String, Set<String>> map : anyUser.entrySet()) {
                            String key = map.getKey();
                            Set<String> value = map.getValue();
                            if (StrUtil.equalsAny(key, method, SaHttpMethod.ALL.name())) {
                                for (String ignore : value) {
                                    if (StrUtil.equals(ignore, path)) {
                                        return true;
                                    }

                                    if (SaPathPatternParserUtil.match(ignore, path)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    })
                    .check(r -> StpUtil.checkLogin());

            // 接口权限
            Map<String, Set<String>> anyone = ignoreProperties.buildAnyone();
            Map<ResourceApiVO, Set<String>> allApi = this.resourceBiz.findAllApiByCache();

            allApi.forEach((api, auth) ->
                    SaRouter.match(api.getUri()).matchMethod(api.getRequestMethod())
                            .notMatch(r -> {
                                String path = SaHolder.getRequest().getRequestPath();
                                String method = SaHolder.getRequest().getMethod();
                                for (Map.Entry<String, Set<String>> map : anyone.entrySet()) {
                                    String key = map.getKey();
                                    Set<String> value = map.getValue();
                                    if (StrUtil.equalsAny(key, method, SaHttpMethod.ALL.name())) {
                                        for (String ignore : value) {
                                            if (StrUtil.equals(ignore, path)) {
                                                return true;
                                            }

                                            if (SaPathPatternParserUtil.match(ignore, path)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                return false;
                            })
                            .check(r -> StpUtil.checkPermissionOr(auth.toArray(String[]::new)))
            );


            if (!ignoreProperties.getNotConfigUriAllow()) {
                String path = SaHolder.getRequest().getRequestPath();
                String method = SaHolder.getRequest().getMethod();
                ResourceApiVO resourceApi = new ResourceApiVO();
                resourceApi.setUri(path);
                resourceApi.setRequestMethod(method);


                if (!ignoreProperties.isIgnoreAnyone(method, path)) {
                    boolean flag = false;
                    for (Map.Entry<ResourceApiVO, Set<String>> map : allApi.entrySet()) {
                        ResourceApiVO key = map.getKey();

                        if (StrUtil.equalsAny(key.getRequestMethod(), method, SaHttpMethod.ALL.name())) {
                            if (StrUtil.equals(key.getUri(), path) || SaPathPatternParserUtil.match(key.getUri(), path)) {
                                flag = true;
                            }
                        }
                    }

                    if (!flag) {
                        throw new NotPermissionException(resourceApi.getUri(), StpUtil.TYPE).setCode(SaErrorCode.CODE_11051);
                    }
                }
            }

        };
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            log.debug("not exec!!! url={}", request.getRequestURL());
            return true;
        }

        boolean flag = super.preHandle(request, response, handler);
        parseToken(request);
        return flag;
    }


    private void parseToken(HttpServletRequest request) {
        // 忽略 token 认证的接口
        if (ignoreProperties.isIgnoreUser(request.getMethod(), request.getRequestURI())) {
            log.debug("access filter not execute");
            return;
        }
        SaSession tokenSession = StpUtil.getSession();
        Long userId = (Long) tokenSession.getLoginId();
        long topCompanyId = tokenSession.getLong(JWT_KEY_TOP_COMPANY_ID);
        long companyId = tokenSession.getLong(JWT_KEY_COMPANY_ID);
        long deptId = tokenSession.getLong(JWT_KEY_DEPT_ID);
        long employeeId = tokenSession.getLong(JWT_KEY_EMPLOYEE_ID);

        //6, 转换，将 token 解析出来的用户身份 和 解码后的tenant、Authorization 重新封装到请求头
        ContextUtil.setUserId(userId);
        ContextUtil.setEmployeeId(employeeId);
        ContextUtil.setCurrentCompanyId(companyId);
        ContextUtil.setCurrentTopCompanyId(topCompanyId);
        ContextUtil.setCurrentDeptId(deptId);
        MDC.put(ContextConstants.USER_ID_HEADER, String.valueOf(userId));
        MDC.put(ContextConstants.EMPLOYEE_ID_HEADER, String.valueOf(employeeId));
    }
}
