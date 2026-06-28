package com.k12.auth.service;

import jakarta.servlet.http.HttpServletRequest;

public interface UserLoginLogService {

    void recordSuccess(Long userId, String username, String loginType, HttpServletRequest request);

    void recordFailure(String username, String loginType, String failReason, HttpServletRequest request);
}
