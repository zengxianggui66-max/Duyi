package com.k12.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.Result;
import com.k12.common.util.RoleUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 管理端接口鉴权：要求已登录且 user.role=admin（staff 入口）
 */
@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        if (!StringUtils.hasText(userId)) {
            writeError(response, 401, "未登录或Token已过期");
            return false;
        }
        if (!RoleUtils.isAdmin(role)) {
            writeError(response, 403, "需要管理员权限");
            return false;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code == 401 ? 401 : 403);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
    }
}
