package com.tangyh.lamp.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.tangyh.basic.exception.BizException;
import com.tangyh.basic.exception.code.ExceptionCode;
import com.tangyh.basic.utils.DateUtils;
import com.tangyh.lamp.gateway.entity.BlockList;
import com.tangyh.lamp.gateway.entity.RateLimiter;
import com.tangyh.lamp.gateway.service.BlockListService;
import com.tangyh.lamp.gateway.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
@RequiredArgsConstructor
public class PreCheckInterceptor extends HandlerInterceptorAdapter {

    private final BlockListService blockListService;
    private final RateLimiterService rateLimiterService;

    private final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            log.debug("not exec!!! url={}", request.getRequestURL());
            return super.preHandle(request, response, handler);
        }

        boolean blockListResult = matchBlockList(request);
        if (blockListResult) {
            throw BizException.wrap(ExceptionCode.UNAUTHORIZED.getCode(), "阻止列表限制，禁止访问");
        }
        boolean rateLimiterResult = matchRateLimiter(request);
        if (rateLimiterResult) {
            throw BizException.wrap(ExceptionCode.UNAUTHORIZED.getCode(), "访问频率超限，请稍后再试");
        }


        return super.preHandle(request, response, handler);
    }


    private String getUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * 不允许访问的列表
     */
    public boolean matchBlockList(HttpServletRequest request) {
        String originUri = getUri(request);
        if (originUri != null) {
            String ip = ServletUtil.getClientIP(request);
            String requestMethod = request.getMethod();
            AtomicBoolean forbid = new AtomicBoolean(false);
            // 阻止访问列表
            Set<Object> blockList = blockListService.findBlockList(ip);
            blockList.addAll(blockListService.findBlockList());

            // 路径和请求方式 能匹配上，且限制区间内则禁用
            doBlockListCheck(forbid, blockList, originUri, requestMethod);

            log.debug("阻止列表验证完成");
            if (forbid.get()) {
                return true;
            }
        } else {
            log.debug("请求地址未正确获取，无法进行阻止列表检查");
        }
        return false;
    }

    private void doBlockListCheck(AtomicBoolean forbid, Set<Object> blockList, String uri, String requestMethod) {
        for (Object o : blockList) {
            BlockList b = (BlockList) o;
            if (!b.getState()) {
                continue;
            }
            if (!PATH_MATCHER.match(b.getRequestUri(), uri)) {
                continue;
            }
            if (!BlockList.METHOD_ALL.equalsIgnoreCase(b.getRequestMethod())
                    && !StrUtil.equalsIgnoreCase(requestMethod, b.getRequestMethod())) {
                continue;
            }
            if (StrUtil.isNotBlank(b.getLimitStart()) && StrUtil.isNotBlank(b.getLimitEnd())) {
                if (DateUtils.between(LocalTime.parse(b.getLimitStart()), LocalTime.parse(b.getLimitEnd()))) {
                    forbid.set(true);
                }
            } else {
                forbid.set(true);
            }
            if (forbid.get()) {
                break;
            }
        }
    }


    /**
     * 限流规则匹配
     *
     * @return
     */
    public boolean matchRateLimiter(HttpServletRequest request) {
        String originUri = getUri(request);
        if (originUri == null) {
            return false;
        }
        String requestMethod = request.getMethod();
        String requestIp = ServletUtil.getClientIP(request);
        RateLimiter rule = rateLimiterService.getRateLimiter(originUri, RateLimiter.METHOD_ALL);
        if (rule == null) {
            rule = rateLimiterService.getRateLimiter(originUri, requestMethod);
        }
        if (rule != null) {
            AtomicBoolean limit = new AtomicBoolean(false);
            boolean result = rateLimiterCheck(limit, rule, originUri, requestIp, requestMethod);
            log.debug("限流验证已完成");
            if (result) {
                return true;
            }
        }
        return false;
    }

    private boolean rateLimiterCheck(AtomicBoolean limit, RateLimiter rule, String uri, String requestIp, String requestMethod) {
        boolean isRateLimiterHit = rule.getState()
                && (RateLimiter.METHOD_ALL.equalsIgnoreCase(rule.getRequestMethod()) || StrUtil.equalsIgnoreCase(requestMethod, rule.getRequestMethod()));
        if (isRateLimiterHit) {
            if (StrUtil.isNotBlank(rule.getLimitStart()) && StrUtil.isNotBlank(rule.getLimitEnd())) {
                if (DateUtils.between(LocalTime.parse(rule.getLimitStart()), LocalTime.parse(rule.getLimitEnd()))) {
                    limit.set(true);
                }
            } else {
                limit.set(true);
            }
        }
        if (limit.get()) {
            String requestUri = uri;
            int count = rateLimiterService.getCurrentRequestCount(requestUri, requestIp);
            if (count == 0) {
                rateLimiterService.setCurrentRequestCount(requestUri, requestIp, rule.getIntervalSec());
            } else if (count >= rule.getCount()) {
                return true;
            } else {
                rateLimiterService.incrCurrentRequestCount(requestUri, requestIp);
            }
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

}
