package com.k12.resource.config;

import com.k12.common.util.JwtUtils;
import com.k12.resource.security.UserIdResolver;
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
 * 直连资源服务时从 JWT 解析用户 ID（不经网关时无 X-User-Id 头）
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtUserIdFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    public JwtUserIdFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (request.getAttribute(UserIdResolver.ATTR_USER_ID) == null
                && !StringUtils.hasText(request.getHeader("X-User-Id"))) {
            String bearer = request.getHeader("Authorization");
            if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);
                if (jwtUtils.validateToken(token)) {
                    try {
                        request.setAttribute(UserIdResolver.ATTR_USER_ID, jwtUtils.getUserId(token));
                    } catch (Exception ignored) {
                        // 无效 token 不阻断请求
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
