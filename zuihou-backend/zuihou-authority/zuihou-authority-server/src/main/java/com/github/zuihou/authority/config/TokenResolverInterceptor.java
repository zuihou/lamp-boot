//package com.github.zuihou.authority.config;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.github.zuihou.auth.client.properties.AuthClientProperties;
//import com.github.zuihou.auth.client.utils.JwtTokenClientUtils;
//import com.github.zuihou.auth.utils.JwtUserInfo;
//import com.github.zuihou.base.R;
//import com.github.zuihou.common.adapter.IgnoreTokenConfig;
//import com.github.zuihou.context.BaseContextConstants;
//import com.github.zuihou.context.BaseContextHandler;
//import com.github.zuihou.exception.BizException;
//import com.github.zuihou.utils.StrHelper;
//import com.github.zuihou.utils.StrPool;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.util.StringUtils;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
///**
// * 解析token
// * 代替zuihou-admin-cloud版本的 TokenContextFilter
// *
// * @author zuihou
// * @date 2019/12/11
// */
//@Slf4j
//public class TokenResolverInterceptor extends HandlerInterceptorAdapter {
//
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        try {
//            if (!(handler instanceof HandlerMethod)) {
//                log.info("not exec!!! url={}", request.getRequestURL());
//                return super.preHandle(request, response, handler);
//            }
//
//
//
//
//        } catch (Exception e) {
//            log.warn("解析token信息时，发生异常. ", e);
//        }
//        return super.preHandle(request, response, handler);
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        BaseContextHandler.remove();
//        super.afterCompletion(request, response, handler, ex);
//    }
//}
