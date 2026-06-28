package com.k12.auth.config;

import com.k12.common.auth.TokenBlacklistService;
import com.k12.common.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 前端开发环境常直连 auth（不经网关），此时无 X-User-Id。
 * 从 Authorization Bearer JWT 解析用户并注入请求头，与网关透传行为一致。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtUserIdHeaderFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    public JwtUserIdHeaderFilter(JwtUtils jwtUtils, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!StringUtils.hasText(request.getHeader("X-User-Id"))) {
            String token = resolveBearerToken(request);
            if (StringUtils.hasText(token)
                    && jwtUtils.isTokenValidAndNotBlacklisted(token, tokenBlacklistService)) {
                try {
                    Long userId = jwtUtils.getUserId(token);
                    String username = jwtUtils.getUsername(token);
                    Object role = jwtUtils.parseToken(token).get("role");

                    MutableHeaderHttpServletRequest wrapped = new MutableHeaderHttpServletRequest(request);
                    wrapped.setHeader("X-User-Id", String.valueOf(userId));
                    if (StringUtils.hasText(username)) {
                        wrapped.setHeader("X-Username", username);
                    }
                    if (role != null) {
                        wrapped.setHeader("X-User-Role", String.valueOf(role));
                    }
                    filterChain.doFilter(wrapped, response);
                    return;
                } catch (Exception ignored) {
                    // 无效 token 不注入，由业务接口返回 401
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private static String resolveBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
