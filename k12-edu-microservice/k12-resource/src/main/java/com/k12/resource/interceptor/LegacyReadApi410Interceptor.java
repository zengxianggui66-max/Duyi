package com.k12.resource.interceptor;

import com.k12.resource.legacy.LegacyReadApi410Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Phase 3I-D D1：旧读 GET 在开关开启时返回 410 Gone，并给出统一读 Location。
 * 注册顺序晚于 {@link LegacyApiUsageInterceptor}，以便仍记录 legacy 命中趋势。
 */
@Component
public class LegacyReadApi410Interceptor implements HandlerInterceptor, Ordered {

    private final LegacyReadApi410Service legacyReadApi410Service;

    public LegacyReadApi410Interceptor(LegacyReadApi410Service legacyReadApi410Service) {
        this.legacyReadApi410Service = legacyReadApi410Service;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!legacyReadApi410Service.is410Enabled()) {
            return true;
        }
        String path = request.getRequestURI();
        if (!legacyReadApi410Service.isDeprecatedLegacyRead(request.getMethod(), path)) {
            return true;
        }
        Optional<String> location = legacyReadApi410Service.resolveReplacementLocation(path);
        if (location.isEmpty()) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_GONE);
        response.setHeader("Location", location.get());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":410,\"message\":\"旧读接口已下线，请改用 Location 指向的统一资源 API\",\"data\":null}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
