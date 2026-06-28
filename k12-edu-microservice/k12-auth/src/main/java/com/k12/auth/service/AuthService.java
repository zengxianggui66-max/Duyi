package com.k12.auth.service;

import com.k12.common.Result;
import com.k12.common.dto.LoginDTO;
import com.k12.common.dto.RegisterDTO;
import com.k12.common.dto.SmsLoginDTO;
import com.k12.common.dto.SmsRegisterDTO;
import com.k12.common.dto.BindPhoneDTO;
import com.k12.common.dto.ChangePasswordDTO;
import com.k12.common.dto.ChangePhoneDTO;
import com.k12.common.dto.SmsSendDTO;
import com.k12.common.dto.UpdateProfileDTO;
import com.k12.common.dto.OAuthCallbackDTO;
import com.k12.common.dto.OAuthCompleteDTO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface AuthService {
    /** 账号密码登录 */
    Result<Map<String, Object>> login(LoginDTO dto, HttpServletRequest request);
    /** 用户名密码注册（注册成功自动登录） */
    Result<Map<String, Object>> register(RegisterDTO dto);
    /** 手机号注册（教师/学生/家长） */
    Result<Map<String, Object>> smsRegister(SmsRegisterDTO dto);
    /** 手机验证码登录 */
    Result<Map<String, Object>> smsLogin(SmsLoginDTO dto, HttpServletRequest request);
    /** 获取当前用户信息 */
    Result<Map<String, Object>> getCurrentUser(Long userId);

    /** 更新个人资料（角色变更时返回新 token） */
    Result<Map<String, Object>> updateProfile(Long userId, UpdateProfileDTO dto);
    /** OAuth登录 / 绑定回调 */
    Result<Map<String, Object>> oauthLogin(OAuthCallbackDTO dto, HttpServletRequest request);

    /** OAuth 新用户补全身份并注册登录 */
    Result<Map<String, Object>> completeOAuthRegister(OAuthCompleteDTO dto);

    /** 当前用户第三方绑定列表 */
    Result<List<Map<String, Object>>> listOAuthBinds(Long userId);

    /** 当前用户第三方绑定相关操作日志（最近 N 条） */
    Result<List<Map<String, Object>>> listOAuthLogs(Long userId, Integer size);

    /** 解绑第三方账号 */
    Result<Void> unbindOAuth(Long userId, String type);

    /** 绑定手机号 */
    Result<Void> bindPhone(Long userId, BindPhoneDTO dto);

    /** 更换手机号（原号 + 新号双验证） */
    Result<Void> changePhone(Long userId, ChangePhoneDTO dto);

    /** 发送短信（按用途校验，登录态敏感操作需 userId） */
    void sendSmsCode(Long userId, SmsSendDTO dto, String ip);

    /** 修改或设置登录密码 */
    Result<Void> changePassword(Long userId, ChangePasswordDTO dto);

    /** 登出（Token 加入黑名单） */
    Result<Void> logout(String token);
}
