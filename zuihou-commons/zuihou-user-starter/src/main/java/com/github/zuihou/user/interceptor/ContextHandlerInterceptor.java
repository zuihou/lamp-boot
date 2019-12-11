package com.github.zuihou.user.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.zuihou.auth.client.properties.AuthClientProperties;
import com.github.zuihou.auth.client.utils.JwtTokenClientUtils;
import com.github.zuihou.auth.utils.JwtUserInfo;
import com.github.zuihou.base.R;
import com.github.zuihou.common.adapter.IgnoreTokenConfig;
import com.github.zuihou.context.BaseContextHandler;
import com.github.zuihou.exception.BizException;
import com.github.zuihou.utils.StrPool;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

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
public class ContextHandlerInterceptor extends HandlerInterceptorAdapter {
    @Value("${spring.profiles.active:dev}")
    protected String profiles;
    @Autowired
    private AuthClientProperties authClientProperties;
    @Autowired
    private JwtTokenClientUtils jwtTokenClientUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            if (!(handler instanceof HandlerMethod)) {
                log.info("not exec!!! url={}", request.getRequestURL());
                return super.preHandle(request, response, handler);
            }

            String uri = request.getRequestURI();
            //获取token， 解析，然后想信息放入 heade
            //1, 获取token
            String userToken = getTokenFromRequest(authClientProperties.getUser().getHeaderName(), request);

            //2, 解析token
            JwtUserInfo userInfo = null;

            //添加测试环境的特殊token
            if (isDev() && StrPool.TEST.equalsIgnoreCase(userToken)) {
                userInfo = new JwtUserInfo(1L, "zuihou", "最后", 1L, 1L);
            }

            try {
                if (!isIgnoreToken(uri) && userInfo == null) {
                    userInfo = jwtTokenClientUtils.getUserInfo(userToken);
                }
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                throw BizException.wrap(R.FAIL_CODE, "解析用户身份失败");
            }

            //3, 将信息放入header
            if (userInfo != null) {
                BaseContextHandler.setUserId(userInfo.getUserId());
                BaseContextHandler.setAccount(userInfo.getAccount());
                BaseContextHandler.setName(userInfo.getName());
                BaseContextHandler.setOrgId(userInfo.getOrgId());
                BaseContextHandler.setStationId(userInfo.getStationId());
            }

        } catch (Exception e) {
            log.warn("解析token信息时，发生异常. ", e);
        }
        return super.preHandle(request, response, handler);
    }


    protected boolean isDev() {
        return !StrPool.PROD.equalsIgnoreCase(profiles);
    }

    /**
     * 忽略应用级token
     *
     * @return
     */
    protected boolean isIgnoreToken(String uri) {
        return IgnoreTokenConfig.isIgnoreToken(uri);
    }

    protected String getTokenFromRequest(String headerName, HttpServletRequest request) {
        String token = request.getHeader(headerName);
        if (StrUtil.isBlank(token)) {
            token = request.getParameter(headerName);
        }
        return token;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContextHandler.remove();
        super.afterCompletion(request, response, handler, ex);
    }

}
