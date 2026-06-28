package com.k12.auth.service;

import com.k12.common.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败次数限制（按用户名或手机号）
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MS = 15 * 60 * 1000;

    private final Map<String, AttemptEntry> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        AttemptEntry entry = attempts.get(key);
        if (entry == null) {
            return;
        }
        if (entry.lockedUntil > Instant.now().toEpochMilli()) {
            long remainSec = (entry.lockedUntil - Instant.now().toEpochMilli()) / 1000;
            throw new BusinessException(429, "登录失败次数过多，请 " + Math.max(remainSec, 1) + " 秒后再试");
        }
        if (entry.lockedUntil > 0 && entry.lockedUntil <= Instant.now().toEpochMilli()) {
            attempts.remove(key);
        }
    }

    public void onFailure(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        attempts.compute(key, (k, old) -> {
            int count = (old == null ? 0 : old.failCount) + 1;
            if (count >= MAX_ATTEMPTS) {
                return new AttemptEntry(count, Instant.now().toEpochMilli() + LOCK_MS);
            }
            return new AttemptEntry(count, 0);
        });
    }

    public void onSuccess(String key) {
        if (key != null) {
            attempts.remove(key);
        }
    }

    private record AttemptEntry(int failCount, long lockedUntil) {
    }
}
