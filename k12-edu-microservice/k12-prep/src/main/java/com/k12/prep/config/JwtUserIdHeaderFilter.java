package com.k12.prep.config;

import com.k12.common.auth.TokenBlacklistService;
import com.k12.common.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class JwtUserIdHeaderFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!StringUtils.hasText(request.getHeader("X-User-Id"))) {
            String token = resolveBearer(request);
            if (StringUtils.hasText(token)
                    && jwtUtils.isTokenValidAndNotBlacklisted(token, tokenBlacklistService)) {
                try {
                    Long userId = jwtUtils.getUserId(token);
                    MutableHeaderHttpServletRequest wrapped = new MutableHeaderHttpServletRequest(request);
                    wrapped.setHeader("X-User-Id", String.valueOf(userId));
                    chain.doFilter(wrapped, response);
                    return;
                } catch (Exception ignored) {
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static String resolveBearer(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
