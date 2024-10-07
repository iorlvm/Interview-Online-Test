package com.apxpert.weien.config;

import com.apxpert.weien.filter.TokenParsingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final TokenParsingInterceptor tokenParsingInterceptor;

    public WebConfig(TokenParsingInterceptor tokenParsingInterceptor) {
        this.tokenParsingInterceptor = tokenParsingInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 解析token的攔截器
        registry.addInterceptor(tokenParsingInterceptor)
                .addPathPatterns("/**");
    }
}
