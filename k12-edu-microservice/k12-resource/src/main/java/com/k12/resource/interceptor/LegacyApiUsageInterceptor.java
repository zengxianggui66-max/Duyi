package com.k12.resource.interceptor;

import com.k12.resource.service.LegacyApiUsageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.core.Ordered;

import java.util.Set;

@Component
public class LegacyApiUsageInterceptor implements HandlerInterceptor, Ordered {

    private static final Set<String> LEGACY_READ_PATHS = Set.of(
            "/api/primary-chinese/page",
            "/api/resources/browse",
            "/api/resources/browse/stats",
            "/api/resources/browse/suites",
            "/api/resources/browse/module-stats",
            "/api/topic/resources/page",
            "/api/culture/resources/page",
            "/api/competition/resources/page"
    );

    private final LegacyApiUsageService legacyApiUsageService;

    public LegacyApiUsageInterceptor(LegacyApiUsageService legacyApiUsageService) {
        this.legacyApiUsageService = legacyApiUsageService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if ("GET".equalsIgnoreCase(request.getMethod()) && LEGACY_READ_PATHS.contains(path)) {
            try {
                legacyApiUsageService.record(path, request.getQueryString());
            } catch (Exception ignored) {
                // 统计失败不影响主请求
            }
        }
        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
