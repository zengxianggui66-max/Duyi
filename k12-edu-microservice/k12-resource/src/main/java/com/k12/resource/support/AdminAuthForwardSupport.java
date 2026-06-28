package com.k12.resource.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

public final class AdminAuthForwardSupport {

    private static final List<String> FORWARD_HEADERS = List.of(
            "Authorization", "X-User-Id", "X-User-Role", "X-Username"
    );

    private AdminAuthForwardSupport() {
    }

    public static HttpHeaders forwardHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return headers;
        }
        HttpServletRequest request = attrs.getRequest();
        for (String name : FORWARD_HEADERS) {
            String value = request.getHeader(name);
            if (StringUtils.hasText(value)) {
                headers.set(name, value);
            }
        }
        return headers;
    }
}
