package com.k12.auth.service;

import com.k12.auth.service.OAuthStateService.OAuthStateContext;

import java.util.List;
import java.util.Map;

/**
 * OAuth 第三方登录 / 绑定服务
 */
public interface OAuthService {

    Map<String, Object> getAuthorizationUrl(String type, String redirectUri, String state);

    Map<String, Object> handleCallback(String code, String type, OAuthStateContext context);

    List<Map<String, Object>> listBinds(Long userId);

    List<Map<String, Object>> listLogs(Long userId, Integer size);

    void unbind(Long userId, String type);

    Map<String, Object> completeOAuthRegister(String type, String oauthId, String nickname,
                                              String avatar, String role);
}
