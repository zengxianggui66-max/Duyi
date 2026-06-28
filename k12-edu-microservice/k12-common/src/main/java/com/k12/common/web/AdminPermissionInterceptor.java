package com.k12.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.service.AdminPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "k12.admin.rbac", name = "enabled", havingValue = "true")
@ConditionalOnBean(AdminPermissionService.class)
public class AdminPermissionInterceptor implements HandlerInterceptor {

    private final AdminPermissionService adminPermissionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequiresPermission annotation = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        }
        if (annotation == null) {
            return true;
        }
        String userIdHeader = request.getHeader("X-User-Id");
        if (!StringUtils.hasText(userIdHeader)) {
            writeError(response, 401, "未登录或Token已过期");
            return false;
        }
        Long userId = Long.parseLong(userIdHeader);
        if (!adminPermissionService.hasPermission(userId, annotation.value())) {
            writeError(response, 403, "无操作权限: " + annotation.value());
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
