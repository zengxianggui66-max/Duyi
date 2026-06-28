package com.k12.auth.service.impl;

import com.k12.auth.mapper.UserLoginLogMapper;
import com.k12.auth.service.UserLoginLogService;
import com.k12.common.entity.UserLoginLog;
import com.k12.common.util.RoleUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserLoginLogServiceImpl implements UserLoginLogService {

    private final UserLoginLogMapper userLoginLogMapper;
    public UserLoginLogServiceImpl(UserLoginLogMapper userLoginLogMapper) {
        this.userLoginLogMapper = userLoginLogMapper;
    }


    @Override
    public void recordSuccess(Long userId, String username, String loginType, HttpServletRequest request) {
        insert(userId, username, loginType, 1, null, request);
    }

    @Override
    public void recordFailure(String username, String loginType, String failReason, HttpServletRequest request) {
        insert(null, username, loginType, 0, failReason, request);
    }

    public static String resolveLoginType(String portalRole, String rawType) {
        if (StringUtils.hasText(rawType)) {
            return rawType;
        }
        if (RoleUtils.isAdmin(portalRole)) {
            return "admin";
        }
        return "password";
    }

    private void insert(Long userId, String username, String loginType, int success,
                        String failReason, HttpServletRequest request) {
        UserLoginLog row = new UserLoginLog();
        row.setUserId(userId);
        row.setUsername(username);
        row.setLoginType(loginType != null ? loginType : "password");
        row.setSuccess(success);
        row.setFailReason(failReason);
        if (request != null) {
            row.setIp(resolveClientIp(request));
            String ua = request.getHeader("User-Agent");
            if (StringUtils.hasText(ua) && ua.length() > 512) {
                ua = ua.substring(0, 512);
            }
            row.setUserAgent(ua);
        }
        userLoginLogMapper.insert(row);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
