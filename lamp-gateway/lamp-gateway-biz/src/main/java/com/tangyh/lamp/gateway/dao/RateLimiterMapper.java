package com.tangyh.lamp.gateway.dao;

import com.tangyh.basic.base.mapper.SuperMapper;
import com.tangyh.lamp.gateway.entity.RateLimiter;
import org.springframework.stereotype.Repository;

/**
 * 限流
 *
 * @author zuihou
 * @date 2020/8/5 上午10:31
 */
@Repository
public interface RateLimiterMapper extends SuperMapper<RateLimiter> {
}
