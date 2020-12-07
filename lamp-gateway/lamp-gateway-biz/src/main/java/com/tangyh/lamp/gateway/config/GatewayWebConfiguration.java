package com.tangyh.lamp.gateway.config;

import com.tangyh.lamp.gateway.filter.PreCheckMvcConfigurer;
import com.tangyh.lamp.gateway.service.BlockListService;
import com.tangyh.lamp.gateway.service.RateLimiterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zuihou
 * @date 2017-12-15 14:42
 */
@Configuration
public class GatewayWebConfiguration {

    @Bean
    public PreCheckMvcConfigurer getPreCheckMvcConfigurer(BlockListService blockListService, RateLimiterService rateLimiterService) {
        return new PreCheckMvcConfigurer(blockListService, rateLimiterService);
    }
}
