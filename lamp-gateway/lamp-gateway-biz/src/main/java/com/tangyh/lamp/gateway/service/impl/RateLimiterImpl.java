package com.tangyh.lamp.gateway.service.impl;


import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.tangyh.basic.base.service.SuperServiceImpl;
import com.tangyh.basic.cache.model.CacheKey;
import com.tangyh.basic.cache.repository.CacheOps;
import com.tangyh.lamp.common.cache.gateway.RateLimiterCacheKeyBuilder;
import com.tangyh.lamp.gateway.dao.RateLimiterMapper;
import com.tangyh.lamp.gateway.entity.RateLimiter;
import com.tangyh.lamp.gateway.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * 限流
 *
 * @author zuihou
 * @date 2020/8/5 上午10:30
 */
@Slf4j
@Service

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RateLimiterImpl extends SuperServiceImpl<RateLimiterMapper, RateLimiter> implements RateLimiterService {

    private final CacheOps cacheOps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(RateLimiter model) {
        int bool = baseMapper.insert(model);
        saveRateLimiter(model);
        return SqlHelper.retBool(bool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(RateLimiter model) {
        removeRateLimiter(model);
        int bool = baseMapper.updateById(model);
        saveRateLimiter(model);
        return SqlHelper.retBool(bool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (idList.isEmpty()) {
            return true;
        }
        List<RateLimiter> rateLimiters = listByIds(idList);
        if (rateLimiters.isEmpty()) {
            return true;
        }
        baseMapper.deleteBatchIds(idList);
        rateLimiters.forEach(this::removeRateLimiter);
        return true;
    }

    @Override
    public void saveRateLimiter(RateLimiter rateLimiter) {
        CacheKey key = new RateLimiterCacheKeyBuilder().key(rateLimiter.getRequestUri(), rateLimiter.getRequestMethod());
        cacheOps.set(key, rateLimiter);
    }

    @Override
    public RateLimiter getRateLimiter(String uri, String method) {
        CacheKey key = new RateLimiterCacheKeyBuilder().key(uri, method);
        return cacheOps.get(key);
    }


    @Override
    public void removeRateLimiter(RateLimiter rateLimiter) {
        CacheKey key = new RateLimiterCacheKeyBuilder().key(rateLimiter.getRequestUri(), rateLimiter.getRequestMethod());
        cacheOps.del(key);
    }

    @Override
    public int getCurrentRequestCount(String uri, String ip) {
        CacheKey key = new RateLimiterCacheKeyBuilder().key(uri, ip);
        return cacheOps.exists(key) ? cacheOps.get(key) : 0;
    }


    @Override
    public void setCurrentRequestCount(String uri, String ip, Long time) {
        CacheKey key = new RateLimiterCacheKeyBuilder().key(uri, ip);
        if (time != null && time > 0L) {
            key.setExpire(Duration.ofSeconds(time));
        }
        cacheOps.set(key, 1);
    }

    @Override
    public void incrCurrentRequestCount(String uri, String ip) {
        CacheKey key = new RateLimiterCacheKeyBuilder().key(uri, ip);
        cacheOps.incr(key);
    }

    @Override
    public void loadAllRateLimiters() {
        List<RateLimiter> list = list();
        list.forEach((rateLimiter) -> {
            CacheKey key = new RateLimiterCacheKeyBuilder().key(rateLimiter.getRequestUri(), rateLimiter.getRequestMethod());
            cacheOps.set(key, rateLimiter);
        });
    }
}
