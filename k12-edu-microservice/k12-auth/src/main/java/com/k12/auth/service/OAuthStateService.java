package com.k12.auth.service;

import com.k12.common.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth state 防 CSRF（5 分钟有效），携带 mode/role/userId 上下文
 */
@Service
public class OAuthStateService {

    public static final String MODE_LOGIN = "login";
    public static final String MODE_BIND = "bind";

    private static final long TTL_MS = 5 * 60 * 1000;
    private final Map<String, StateEntry> store = new ConcurrentHashMap<>();

    public String createLoginState(String type, String role) {
        return create(type, MODE_LOGIN, role, null);
    }

    public String createBindState(String type, Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录后再绑定第三方账号");
        }
        return create(type, MODE_BIND, null, userId);
    }

    private String create(String type, String mode, String role, Long userId) {
        String state = UUID.randomUUID().toString().replace("-", "");
        store.put(state, new StateEntry(type, mode, role, userId, Instant.now().toEpochMilli() + TTL_MS));
        return state;
    }

    public OAuthStateContext validateAndConsume(String state, String type) {
        if (state == null || state.isBlank()) {
            throw new BusinessException(400, "缺少 state 参数");
        }
        StateEntry entry = store.remove(state);
        if (entry == null) {
            throw new BusinessException(400, "state 无效或已过期");
        }
        if (entry.expireAt < Instant.now().toEpochMilli()) {
            throw new BusinessException(400, "state 已过期，请重新发起授权");
        }
        if (!entry.type.equals(type)) {
            throw new BusinessException(400, "state 与登录类型不匹配");
        }
        return new OAuthStateContext(entry.type, entry.mode, entry.role, entry.userId);
    }

    public record OAuthStateContext(String type, String mode, String role, Long userId) {
        public boolean isBindMode() {
            return MODE_BIND.equals(mode);
        }

        public boolean isLoginMode() {
            return MODE_LOGIN.equals(mode);
        }
    }

    private record StateEntry(String type, String mode, String role, Long userId, long expireAt) {
    }
}
