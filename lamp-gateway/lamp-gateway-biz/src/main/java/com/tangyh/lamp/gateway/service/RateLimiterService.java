package com.tangyh.lamp.gateway.service;

import com.tangyh.basic.base.service.SuperService;
import com.tangyh.lamp.gateway.entity.RateLimiter;

/**
 * 限流
 *
 * @author zuihou
 * @date 2020/8/5 上午10:30
 */
public interface RateLimiterService extends SuperService<RateLimiter> {
    /**
     * 缓存限流规则
     *
     * @param rateLimiter 限流规则
     */
    void saveRateLimiter(RateLimiter rateLimiter);

    /**
     * 从缓存中获取限流规则
     *
     * @param uri    uri
     * @param method method
     * @return 限流规则
     */
    RateLimiter getRateLimiter(String uri, String method);

    /**
     * 获取当前请求次数
     *
     * @param uri uri
     * @param ip  ip
     * @return 次数
     */
    int getCurrentRequestCount(String uri, String ip);

    /**
     * 从缓存中删除限流规则
     *
     * @param rateLimiter 限流规则
     */
    void removeRateLimiter(RateLimiter rateLimiter);

    /**
     * 设置请求次数
     *
     * @param uri  uri
     * @param ip   ip
     * @param time 有效期
     */
    void setCurrentRequestCount(String uri, String ip, Long time);

    /**
     * 递增请求次数
     *
     * @param uri uri
     * @param ip  ip
     */
    void incrCurrentRequestCount(String uri, String ip);

    /**
     * 加载所有的限流规则写入到缓存
     */
    void loadAllRateLimiters();
}
