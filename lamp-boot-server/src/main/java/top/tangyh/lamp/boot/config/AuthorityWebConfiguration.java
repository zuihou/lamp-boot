package top.tangyh.lamp.boot.config;

import top.tangyh.basic.boot.config.BaseConfig;
import top.tangyh.basic.cache.repository.CacheOps;
import top.tangyh.basic.database.properties.DatabaseProperties;
import top.tangyh.basic.jwt.TokenUtil;
import top.tangyh.basic.log.event.SysLogListener;
import top.tangyh.lamp.boot.interceptor.TokenHandlerInterceptor;
import top.tangyh.lamp.boot.ext.UserResolverServiceImpl;
import top.tangyh.basic.security.feign.UserResolverService;
import top.tangyh.basic.security.properties.SecurityProperties;
import top.tangyh.lamp.authority.service.auth.UserService;
import top.tangyh.lamp.authority.service.common.OptLogService;
import top.tangyh.lamp.common.properties.IgnoreProperties;
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
import top.tangyh.lamp.common.properties.SystemProperties;

/**
 * @author zuihou
 * @date 2017-12-15 14:42
 */
@Configuration
@EnableConfigurationProperties({IgnoreProperties.class, SystemProperties.class})
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
