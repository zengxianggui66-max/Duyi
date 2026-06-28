package com.k12.auth.controller;

import com.k12.common.BusinessException;
import com.k12.common.Result;
import com.k12.common.dto.BindPhoneDTO;
import com.k12.common.dto.LoginDTO;
import com.k12.common.dto.OAuthCallbackDTO;
import com.k12.common.dto.OAuthCompleteDTO;
import com.k12.common.dto.RegisterDTO;
import com.k12.common.dto.SmsLoginDTO;
import com.k12.common.dto.SmsRegisterDTO;
import com.k12.common.dto.SmsSendDTO;
import com.k12.common.dto.ChangePasswordDTO;
import com.k12.common.dto.ChangePhoneDTO;
import com.k12.common.dto.UpdateProfileDTO;
import com.k12.auth.service.AuthService;
import com.k12.auth.service.CaptchaService;
import com.k12.auth.service.OAuthService;
import com.k12.auth.service.OAuthStateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;
    private final OAuthStateService oAuthStateService;
    private final CaptchaService captchaService;
    public AuthController(AuthService authService, OAuthService oAuthService, OAuthStateService oAuthStateService, CaptchaService captchaService) {
        this.authService = authService;
        this.oAuthService = oAuthService;
        this.oAuthStateService = oAuthStateService;
        this.captchaService = captchaService;
    }


    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return authService.login(dto, request);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/sms/register")
    public Result<Map<String, Object>> smsRegister(@Valid @RequestBody SmsRegisterDTO dto) {
        return authService.smsRegister(dto);
    }

    @GetMapping("/captcha")
    public Result<Map<String, Object>> getCaptcha() {
        return Result.success(captchaService.generate());
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody SmsSendDTO dto,
            HttpServletRequest request) {
        captchaService.requireValid(dto.getCaptchaKey(), dto.getCaptchaCode());
        String ip = getClientIp(request);
        authService.sendSmsCode(userId, dto, ip);
        return Result.success("验证码已发送", null);
    }

    @PostMapping("/sms/login")
    public Result<Map<String, Object>> smsLogin(@Valid @RequestBody SmsLoginDTO dto, HttpServletRequest request) {
        return authService.smsLogin(dto, request);
    }

    @GetMapping("/oauth/url")
    public Result<Map<String, Object>> getOAuthUrl(
            @RequestParam String type,
            @RequestParam(defaultValue = "http://localhost:5173/login/oauth/callback") String redirectUri,
            @RequestParam(defaultValue = "login") String mode,
            @RequestParam(required = false) String role) {
        String state;
        if ("bind".equals(mode)) {
            throw new BusinessException(400, "绑定请使用 /oauth/bind-url 接口");
        }
        state = oAuthStateService.createLoginState(type, role);
        Map<String, Object> data = new LinkedHashMap<>(oAuthService.getAuthorizationUrl(type, redirectUri, state));
        data.put("state", state);
        return Result.success(data);
    }

    @GetMapping("/oauth/bind-url")
    public Result<Map<String, Object>> getOAuthBindUrl(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String type,
            @RequestParam(defaultValue = "http://localhost:5173/login/oauth/callback") String redirectUri) {
        String state = oAuthStateService.createBindState(type, userId);
        Map<String, Object> data = new LinkedHashMap<>(oAuthService.getAuthorizationUrl(type, redirectUri, state));
        data.put("state", state);
        return Result.success(data);
    }

    @PostMapping("/oauth/login")
    public Result<Map<String, Object>> oauthLogin(
            @Valid @RequestBody OAuthCallbackDTO dto,
            HttpServletRequest request) {
        return authService.oauthLogin(dto, request);
    }

    @PostMapping("/oauth/complete")
    public Result<Map<String, Object>> completeOAuth(@Valid @RequestBody OAuthCompleteDTO dto) {
        return authService.completeOAuthRegister(dto);
    }

    @GetMapping("/oauth/binds")
    public Result<java.util.List<Map<String, Object>>> listOAuthBinds(
            @RequestHeader("X-User-Id") Long userId) {
        return authService.listOAuthBinds(userId);
    }

    @GetMapping("/oauth/logs")
    public Result<java.util.List<Map<String, Object>>> listOAuthLogs(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        return authService.listOAuthLogs(userId, size);
    }

    @DeleteMapping("/oauth/bind/{type}")
    public Result<Void> unbindOAuth(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String type) {
        return authService.unbindOAuth(userId, type);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.logout(extractBearerToken(authorization));
    }

    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BindPhoneDTO dto) {
        return authService.bindPhone(userId, dto);
    }

    @PostMapping("/change-phone")
    public Result<Void> changePhone(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody ChangePhoneDTO dto) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return authService.changePhone(userId, dto);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody ChangePasswordDTO dto) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return authService.changePassword(userId, dto);
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return authService.getCurrentUser(userId);
    }

    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody UpdateProfileDTO dto) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return authService.updateProfile(userId, dto);
    }

    // ========== 管理端用户接口已迁移至 AdminUserController (/api/admin/users) ==========

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
