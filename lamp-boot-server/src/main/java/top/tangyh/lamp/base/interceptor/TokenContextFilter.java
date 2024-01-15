package top.tangyh.lamp.base.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.tangyh.basic.cache.redis2.CacheResult;
import top.tangyh.basic.cache.repository.CacheOps;
import top.tangyh.basic.context.ContextConstants;
import top.tangyh.basic.context.ContextUtil;
import top.tangyh.basic.exception.UnauthorizedException;
import top.tangyh.basic.jwt.TokenHelper;
import top.tangyh.basic.jwt.model.Token;
import top.tangyh.basic.jwt.utils.Base64Util;
import top.tangyh.basic.model.cache.CacheKey;
import top.tangyh.basic.utils.StrPool;
import top.tangyh.lamp.common.cache.common.TokenUserIdCacheKeyBuilder;
import top.tangyh.lamp.common.constant.BizConstant;
import top.tangyh.lamp.common.properties.IgnoreProperties;


import static top.tangyh.basic.context.ContextConstants.*;
import static top.tangyh.basic.exception.code.ExceptionCode.JWT_NOT_LOGIN;
import static top.tangyh.basic.exception.code.ExceptionCode.JWT_OFFLINE;

/**
 * 用户信息解析器 一定要在AuthenticationFilter之前执行
 *
 * @author tangyh
 * @version v1.0
 * @date 2021/12/28 2:36 下午
 * @create [2021/12/28 2:36 下午 ] [tangyh] [初始创建]
 */
@Slf4j
@RequiredArgsConstructor
public class TokenContextFilter implements AsyncHandlerInterceptor {
    private final String profiles;
    private final IgnoreProperties ignoreProperties;
    private final TokenHelper tokenUtil;
    private final CacheOps cacheOps;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            log.debug("not exec!!! url={}", request.getRequestURL());
            return true;
        }
        ContextUtil.setBoot(true);
        ContextUtil.setPath(getHeader(ContextConstants.PATH_HEADER, request));
        String traceId = IdUtil.fastSimpleUUID();
        MDC.put(ContextConstants.TRACE_ID_HEADER, traceId);
        try {
            // 1,解码 Authorization
            parseClient(request);

            // 2, 获取 应用id
            parseApplication(request);

            // 3，解析 Token
            parseToken(request);
        } catch (Exception e) {
            log.error("request={}", request.getRequestURL(), e);
            throw e;
        }

        return true;
    }

    private boolean parseToken(HttpServletRequest request) {
        // 忽略 token 认证的接口
        if (isIgnoreToken(request)) {
            log.debug("access filter not execute");
            return true;
        }

        //3, 获取token
        String token = getHeader(TOKEN_KEY, request);


        Token tokenObj;
        //添加测试环境的特殊token
        if (isDev(token)) {
            tokenObj = new Token();
            tokenObj.setUserId(1L).setEmployeeId(1L).setCurrentTopCompanyId(1L).setCurrentCompanyId(1L).setCurrentDeptId(1L);
        } else {
            tokenObj = tokenUtil.parseToken(token);

            // 验证 是否在其他设备登录或被挤下线
            // TOKEN_USER_ID:{token} === T
            CacheKey cacheKey = TokenUserIdCacheKeyBuilder.builder(tokenObj.getUuid());
            CacheResult<String> tokenCache = cacheOps.get(cacheKey);

            if (StrUtil.isEmpty(tokenCache.getValue())) {
                log.error("token is empty");
                throw UnauthorizedException.wrap(JWT_NOT_LOGIN.getMsg());
            } else if (StrUtil.equals(BizConstant.LOGIN_STATUS, tokenCache.getValue())) {
                log.error("您被踢了");
                throw UnauthorizedException.wrap(JWT_OFFLINE.getMsg());
            }
        }

        //6, 转换，将 token 解析出来的用户身份 和 解码后的tenant、Authorization 重新封装到请求头
        ContextUtil.setUserId(tokenObj.getUserId());
        ContextUtil.setEmployeeId(tokenObj.getEmployeeId());
        ContextUtil.setCurrentCompanyId(tokenObj.getCurrentCompanyId());
        ContextUtil.setCurrentTopCompanyId(tokenObj.getCurrentTopCompanyId());
        ContextUtil.setCurrentDeptId(tokenObj.getCurrentDeptId());
        MDC.put(ContextConstants.USER_ID_HEADER, String.valueOf(tokenObj.getUserId()));
        MDC.put(ContextConstants.EMPLOYEE_ID_HEADER, String.valueOf(tokenObj.getEmployeeId()));

        return false;
    }

    private void parseClient(HttpServletRequest request) {
        String base64Authorization = getHeader(CLIENT_KEY, request);
        if (StrUtil.isNotEmpty(base64Authorization)) {
            String[] client = Base64Util.getClient(base64Authorization);
            ContextUtil.setClientId(client[0]);
        }
    }

    private void parseApplication(HttpServletRequest request) {
        String applicationIdStr = getHeader(APPLICATION_ID_KEY, request);
        if (StrUtil.isNotEmpty(applicationIdStr)) {
            ContextUtil.setApplicationId(applicationIdStr);
            MDC.put(APPLICATION_ID_HEADER, applicationIdStr);
        }
    }

    private String getHeader(String name, HttpServletRequest request) {
        String value = request.getHeader(name);
        if (StrUtil.isEmpty(value)) {
            value = request.getParameter(name);
        }
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        return URLUtil.decode(value);
    }


    protected boolean isDev(String token) {
        return !StrPool.PROD.equalsIgnoreCase(profiles) && (StrPool.TEST_TOKEN.equalsIgnoreCase(token) || StrPool.TEST.equalsIgnoreCase(token));
    }

    /**
     * 忽略应用级token
     *
     * @return
     */
    protected boolean isIgnoreToken(HttpServletRequest request) {
        return ignoreProperties.isIgnoreUser(request.getMethod(), request.getRequestURI());
    }

    /**
     * 忽略 租户编码
     *
     * @return
     */
    protected boolean isIgnoreTenant(HttpServletRequest request) {
        return ignoreProperties.isIgnoreTenant(request.getMethod(), request.getRequestURI());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextUtil.remove();
    }
}
