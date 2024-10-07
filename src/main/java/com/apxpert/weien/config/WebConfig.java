package com.apxpert.weien.config;

import com.apxpert.weien.filter.TokenParsingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final TokenParsingInterceptor tokenParsingInterceptor;

    public WebConfig(TokenParsingInterceptor tokenParsingInterceptor) {
        this.tokenParsingInterceptor = tokenParsingInterceptor;
    }

    private CorsConfiguration corsConfig() {
        // 跨域請求設定 未來確定規格後要修正
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOrigin("http://localhost:5173");  // TODO: 上線後要修改
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        // Spring Boot的跨域請求 (寫法不一樣)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig());
        return new CorsFilter(source);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 解析token的攔截器
        registry.addInterceptor(tokenParsingInterceptor)
                .addPathPatterns("/**");
    }
}
