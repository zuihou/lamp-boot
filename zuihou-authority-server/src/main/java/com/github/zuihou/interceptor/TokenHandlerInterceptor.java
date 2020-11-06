package com.github.zuihou.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.github.zuihou.base.R;
import com.github.zuihou.common.constant.BizConstant;
import com.github.zuihou.common.constant.CacheKey;
import com.github.zuihou.common.properties.IgnoreProperties;
import com.github.zuihou.context.BaseContextConstants;
import com.github.zuihou.context.BaseContextHandler;
import com.github.zuihou.database.properties.DatabaseProperties;
import com.github.zuihou.database.properties.MultiTenantType;
import com.github.zuihou.exception.BizException;
import com.github.zuihou.jwt.TokenUtil;
import com.github.zuihou.jwt.model.AuthInfo;
import com.github.zuihou.jwt.utils.JwtUtil;
import com.github.zuihou.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.zuihou.context.BaseContextConstants.BASIC_HEADER_KEY;
import static com.github.zuihou.context.BaseContextConstants.BEARER_HEADER_KEY;
import static com.github.zuihou.context.BaseContextConstants.JWT_KEY_TENANT;
import static com.github.zuihou.exception.code.ExceptionCode.JWT_OFFLINE;

/**
 * 网关：
 * 获取token，并解析，然后将所有的用户、应用信息封装到请求头
 * <p>
 * 拦截器：
 * 解析请求头数据， 将用户信息、应用信息封装到BaseContextHandler
 * 考虑请求来源是否网关（ip等）
 * <p>
 * Created by zuihou on 2017/9/10.
 *
 * @author zuihou
 * @date 2019-06-20 22:22
 */
@Slf4j
public class TokenHandlerInterceptor extends HandlerInterceptorAdapter {

    @Value("${spring.profiles.active:dev}")
    protected String profiles;
    private final IgnoreProperties ignoreTokenProperties;
    private final DatabaseProperties databaseProperties;

    @Autowired
    private CacheChannel channel;
    @Autowired
    private TokenUtil tokenUtil;

    public TokenHandlerInterceptor(IgnoreProperties ignoreTokenProperties, DatabaseProperties databaseProperties) {
        this.ignoreTokenProperties = ignoreTokenProperties;
        this.databaseProperties = databaseProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            log.info("not exec!!! url={}", request.getRequestURL());
            return super.preHandle(request, response, handler);
        }
        BaseContextHandler.setBoot(true);
        String traceId = IdUtil.fastSimpleUUID();
        MDC.put(BaseContextConstants.LOG_TRACE_ID, traceId);
        try {
            //1, 解码 请求头中的租户信息
            parseTenant(request);

            // 2,解码 Authorization 后面完善
            parseClient(request);

            // 3，解析token
            parseToken(request);
        } catch (BizException e) {
            throw BizException.wrap(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw BizException.wrap(R.FAIL_CODE, "验证token出错");
        }

        return super.preHandle(request, response, handler);
    }

    private boolean parseToken(HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();
        // 忽略 token 认证的接口
        if (isIgnoreToken(uri)) {
            log.debug("access filter not execute");
            return true;
        }

        //获取token， 解析，然后想信息放入 heade
        //3, 获取token
        String token = getHeader(BEARER_HEADER_KEY, request);

        AuthInfo authInfo;
        //添加测试环境的特殊token
        if (isDev(token)) {
            authInfo = new AuthInfo().setAccount("zuihou").setUserId(3L).setTokenType(BEARER_HEADER_KEY).setName("平台管理员");
        } else {
            authInfo = tokenUtil.getAuthInfo(token);

            // 5，验证 是否在其他设备登录或被挤下线
            String newToken = JwtUtil.getToken(token);
            String tokenKey = CacheKey.buildKey(newToken);
            CacheObject tokenCache = channel.get(CacheKey.TOKEN_USER_ID, tokenKey);
            if (tokenCache.getValue() == null) {
                // 为空就认为是没登录或者被T会有bug，该 bug 取决于登录成功后，异步调用UserTokenService.save 方法的延迟
            } else if (StrUtil.equals(BizConstant.LOGIN_STATUS, (String) tokenCache.getValue())) {
                throw BizException.wrap(JWT_OFFLINE);
            }
        }


        //6, 转换，将 token 解析出来的用户身份 和 解码后的tenant、Authorization 重新封装到请求头
        if (authInfo != null) {
            BaseContextHandler.setUserId(authInfo.getUserId());
            BaseContextHandler.setAccount(authInfo.getAccount());
            BaseContextHandler.setName(authInfo.getName());
            MDC.put(BaseContextConstants.JWT_KEY_USER_ID, String.valueOf(authInfo.getUserId()));
        }
        return false;
    }

    private void parseClient(HttpServletRequest request) {
        String base64Authorization = getHeader(BASIC_HEADER_KEY, request);
        if (StrUtil.isNotEmpty(base64Authorization)) {
            String[] client = JwtUtil.getClient(base64Authorization);
            BaseContextHandler.setClientId(client[0]);
        }
    }

    /**
     * 忽略 租户编码
     *
     * @return
     */
    protected boolean isIgnoreTenant(String path) {
        return MultiTenantType.NONE.eq(databaseProperties.getMultiTenantType()) || ignoreTokenProperties.isIgnoreTenant(path);
    }

    private void parseTenant(HttpServletRequest request) {
        if (isIgnoreTenant(request.getRequestURI())) {
            return;
        }
        String base64Tenant = getHeader(JWT_KEY_TENANT, request);
        if (StrUtil.isNotEmpty(base64Tenant)) {
            String tenant = JwtUtil.base64Decoder(base64Tenant);
            BaseContextHandler.setTenant(tenant);
            MDC.put(BaseContextConstants.JWT_KEY_TENANT, BaseContextHandler.getTenant());
        }
    }

    private String getHeader(String name, HttpServletRequest request) {
        String value = request.getHeader(name);
        if (StringUtils.isEmpty(value)) {
            value = request.getParameter(name);
        }
        if (StringUtils.isEmpty(value)) {
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
    protected boolean isIgnoreToken(String uri) {
        return ignoreTokenProperties.isIgnoreToken(uri);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContextHandler.remove();
        super.afterCompletion(request, response, handler, ex);
    }

}
