package com.k12.resource.security;

import com.k12.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 从请求头 X-User-Id（网关透传）或 Authorization Bearer 解析当前用户 ID
 */
@Component
public class UserIdResolver {

    public static final String ATTR_USER_ID = "currentUserId";

    private final JwtUtils jwtUtils;
    public UserIdResolver(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }


    public Long resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attr = request.getAttribute(ATTR_USER_ID);
        if (attr instanceof Long id) {
            return id;
        }
        if (attr instanceof String s && StringUtils.hasText(s)) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        String headerUserId = request.getHeader("X-User-Id");
        if (StringUtils.hasText(headerUserId)) {
            try {
                return Long.parseLong(headerUserId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        String token = resolveBearerToken(request);
        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            return jwtUtils.getUserId(token);
        }
        return null;
    }

    public String resolveBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
