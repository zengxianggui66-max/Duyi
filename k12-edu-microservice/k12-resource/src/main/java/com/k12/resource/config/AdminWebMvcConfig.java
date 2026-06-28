package com.k12.resource.config;

import com.k12.common.web.AdminAccessInterceptor;
import com.k12.common.web.AdminPermissionInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminWebMvcConfig implements WebMvcConfigurer {

    private final AdminAccessInterceptor adminAccessInterceptor;
    private final ObjectProvider<AdminPermissionInterceptor> adminPermissionInterceptorProvider;
    public AdminWebMvcConfig(AdminAccessInterceptor adminAccessInterceptor, ObjectProvider<AdminPermissionInterceptor> adminPermissionInterceptorProvider) {
        this.adminAccessInterceptor = adminAccessInterceptor;
        this.adminPermissionInterceptorProvider = adminPermissionInterceptorProvider;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAccessInterceptor)
                .addPathPatterns("/api/admin/**");
        adminPermissionInterceptorProvider.ifAvailable(interceptor ->
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/api/admin/**"));
    }
}
