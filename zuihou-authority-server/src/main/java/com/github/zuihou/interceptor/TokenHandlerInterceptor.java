package com.github.zuihou.interceptor;

import cn.hutool.core.util.URLUtil;
import com.github.zuihou.auth.client.properties.AuthClientProperties;
import com.github.zuihou.auth.client.utils.JwtTokenClientUtils;
import com.github.zuihou.auth.utils.JwtUserInfo;
import com.github.zuihou.base.R;
import com.github.zuihou.common.adapter.IgnoreTokenConfig;
import com.github.zuihou.context.BaseContextHandler;
import com.github.zuihou.exception.BizException;
import com.github.zuihou.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            String userToken = getHeader(request, authClientProperties.getUser().getHeaderName());

            //2, 解析token
            JwtUserInfo userInfo = null;

            //添加测试环境的特殊token
            if (isDev() && StrPool.TEST.equalsIgnoreCase(userToken)) {
                userInfo = new JwtUserInfo(1L, "zuihou", "最后");
            }

            if (!isIgnoreToken(uri) && userInfo == null) {
                userInfo = jwtTokenClientUtils.getUserInfo(userToken);
            }

            BaseContextHandler.setBoot(true);

            //3, 将信息放入header
            if (userInfo != null) {
                BaseContextHandler.setUserId(userInfo.getUserId());
                BaseContextHandler.setAccount(userInfo.getAccount());
                BaseContextHandler.setName(userInfo.getName());
            }

        } catch (BizException e) {
            log.error("解析token失败", e);
            throw e;
        } catch (Exception e) {
            log.error("解析token失败", e);
            throw BizException.wrap(R.FAIL_CODE, "解析用户身份失败");
        }
        return super.preHandle(request, response, handler);
    }

    private String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return URLUtil.decode(value);
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContextHandler.remove();
        super.afterCompletion(request, response, handler, ex);
    }

}
