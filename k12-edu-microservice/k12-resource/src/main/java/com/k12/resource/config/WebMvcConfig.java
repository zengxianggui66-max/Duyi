package com.k12.resource.config;

import com.k12.resource.interceptor.LegacyApiUsageInterceptor;
import com.k12.resource.interceptor.LegacyReadApi410Interceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:${user.home}/k12-uploads}")
    private String uploadPath;
    private final LegacyApiUsageInterceptor legacyApiUsageInterceptor;
    private final LegacyReadApi410Interceptor legacyReadApi410Interceptor;

    public WebMvcConfig(LegacyApiUsageInterceptor legacyApiUsageInterceptor,
                        LegacyReadApi410Interceptor legacyReadApi410Interceptor) {
        this.legacyApiUsageInterceptor = legacyApiUsageInterceptor;
        this.legacyReadApi410Interceptor = legacyReadApi410Interceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(legacyApiUsageInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(legacyReadApi410Interceptor).addPathPatterns("/api/**");
    }
}
