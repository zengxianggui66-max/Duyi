package com.k12.common.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 黑名单（内存实现，单机有效；集群环境建议换 Redis）
 */
@Component
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void add(String jti, long expireAtEpochMs) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        blacklist.put(jti, expireAtEpochMs);
        cleanup();
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Long expireAt = blacklist.get(jti);
        if (expireAt == null) {
            return false;
        }
        if (expireAt < Instant.now().toEpochMilli()) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }

    private void cleanup() {
        long now = Instant.now().toEpochMilli();
        blacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}
