package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    // ==================== 跨域 ====================

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")           // 匹配所有接口
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("*")         // 允许所有请求方法
                .allowedHeaders("*")         // 允许所有请求头
                .allowCredentials(true)      // 允许携带凭证
                .maxAge(3600);               // 预检请求缓存 1 小时
    }

    // ==================== 认证拦截 ====================

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/cart/**",
                        "/api/orders/**",
                        "/api/products"      // POST 新增
                )
                .addPathPatterns(
                        "/api/products/*"    // PUT/DELETE 修改删除（不拦截 GET）
                );
    }
}
