package com.tangyh.lamp.boot.config;

import com.tangyh.basic.boot.config.BaseConfig;
import com.tangyh.basic.cache.repository.CacheOps;
import com.tangyh.basic.database.properties.DatabaseProperties;
import com.tangyh.basic.jwt.TokenUtil;
import com.tangyh.basic.log.event.SysLogListener;
import com.tangyh.lamp.boot.interceptor.TokenHandlerInterceptor;
import com.tangyh.lamp.boot.ext.UserResolverServiceImpl;
import com.tangyh.basic.security.feign.UserResolverService;
import com.tangyh.basic.security.properties.SecurityProperties;
import com.tangyh.lamp.authority.service.auth.UserService;
import com.tangyh.lamp.authority.service.common.OptLogService;
import com.tangyh.lamp.common.properties.IgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zuihou
 * @date 2017-12-15 14:42
 */
@Configuration
@EnableConfigurationProperties({IgnoreProperties.class})
@RequiredArgsConstructor
public class AuthorityWebConfiguration extends BaseConfig implements WebMvcConfigurer {

    private final IgnoreProperties ignoreTokenProperties;
    private final DatabaseProperties databaseProperties;
    private final TokenUtil tokenUtil;
    private final CacheOps cacheOps;
    @Value("${spring.profiles.active:dev}")
    protected String profiles;

    @Bean
    public HandlerInterceptor getTokenHandlerInterceptor() {
        return new TokenHandlerInterceptor(profiles, ignoreTokenProperties, databaseProperties,
                tokenUtil, cacheOps);
    }

    /**
     * 注册 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] commonPathPatterns = getExcludeCommonPathPatterns();
        registry.addInterceptor(getTokenHandlerInterceptor())
                .addPathPatterns("/**")
                .order(5)
                .excludePathPatterns(commonPathPatterns);
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    /**
     * auth-client 中的拦截器需要排除拦截的地址
     */
    protected String[] getExcludeCommonPathPatterns() {
        return new String[]{
                "/*.css",
                "/*.js",
                "/*.html",
                "/error",
                "/login",
                "/v2/api-docs",
                "/v2/api-docs-ext",
                "/swagger-resources/**",
                "/webjars/**",

                "/",
                "/csrf",

                "/META-INF/resources/**",
                "/resources/**",
                "/static/**",
                "/public/**",
                "classpath:/META-INF/resources/**",
                "classpath:/resources/**",
                "classpath:/static/**",
                "classpath:/public/**",

                "/cache/**",
                "/swagger-ui.html**",
                "/doc.html**"
        };
    }


    @Bean
    @ConditionalOnProperty(prefix = SecurityProperties.PREFIX, name = "type", havingValue = "SERVICE", matchIfMissing = true)
    public UserResolverService getUserResolverServiceImpl(UserService userService) {
        return new UserResolverServiceImpl(userService);
    }

    /**
     * lamp.log.enabled = true 并且 lamp.log.type=DB时实例该类
     */
    @Bean
    @ConditionalOnExpression("${lamp.log.enabled:true} && 'DB'.equals('${lamp.log.type:LOGGER}')")
    public SysLogListener sysLogListener(OptLogService optLogService) {
        return new SysLogListener(optLogService::save);
    }
}
