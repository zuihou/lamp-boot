package com.github.zuihou.authority.config;

import com.github.zuihou.authority.ext.UserResolverServiceImpl;
import com.github.zuihou.authority.service.auth.UserService;
import com.github.zuihou.authority.service.common.OptLogService;
import com.github.zuihou.boot.config.BaseConfig;
import com.github.zuihou.common.properties.IgnoreProperties;
import com.github.zuihou.database.properties.DatabaseProperties;
import com.github.zuihou.interceptor.TokenHandlerInterceptor;
import com.github.zuihou.log.event.SysLogListener;
import com.github.zuihou.security.feign.UserResolverService;
import com.github.zuihou.security.properties.SecurityProperties;
import lombok.AllArgsConstructor;
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
 * @createTime 2017-12-15 14:42
 */
@Configuration
@EnableConfigurationProperties({IgnoreProperties.class})
@AllArgsConstructor
public class AuthorityWebConfiguration extends BaseConfig implements WebMvcConfigurer {

    private final IgnoreProperties ignoreTokenProperties;
    private final DatabaseProperties databaseProperties;

    @Bean
    public HandlerInterceptor getTokenHandlerInterceptor() {
        return new TokenHandlerInterceptor(ignoreTokenProperties, databaseProperties);
    }

    /**
     * 注册 拦截器
     *
     * @param registry
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
     *
     * @return
     */
    protected String[] getExcludeCommonPathPatterns() {
        String[] urls = {
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
        return urls;
    }

    @Bean
    @ConditionalOnExpression("${zuihou.log.enabled:true} && 'DB'.equals('${zuihou.log.type:LOGGER}')")
    public SysLogListener sysLogListener(OptLogService optLogService) {
        return new SysLogListener((log) -> optLogService.save(log));
    }

    @Bean
    @ConditionalOnProperty(prefix = SecurityProperties.PREFIX, name = "type", havingValue = "SERVICE", matchIfMissing = true)
    public UserResolverService getUserResolverServiceImpl(UserService userService) {
        return new UserResolverServiceImpl(userService);
    }

}

