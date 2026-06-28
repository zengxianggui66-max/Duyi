package com.k12.auth.service.impl;

import com.k12.auth.service.CaptchaService;
import com.k12.common.BusinessException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 算术图形验证码（开发友好，可替换为腾讯云/阿里云 captcha）
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final long TTL_MS = 5 * 60 * 1000;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> generate() {
        int a = random.nextInt(9) + 1;
        int b = random.nextInt(9) + 1;
        String key = UUID.randomUUID().toString().replace("-", "");
        store.put(key, new CaptchaEntry(a + b, Instant.now().toEpochMilli() + TTL_MS));
        return Map.of(
                "captchaKey", key,
                "question", a + " + " + b + " = ?",
                "captchaType", "arithmetic"
        );
    }

    @Override
    public boolean verify(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.isBlank()
                || captchaCode == null || captchaCode.isBlank()) {
            return false;
        }
        CaptchaEntry entry = store.remove(captchaKey);
        if (entry == null) {
            return false;
        }
        if (entry.expireAt < Instant.now().toEpochMilli()) {
            return false;
        }
        try {
            return entry.answer == Integer.parseInt(captchaCode.trim());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void requireValid(String captchaKey, String captchaCode) {
        if (!verify(captchaKey, captchaCode)) {
            throw new BusinessException(400, "图形验证码错误或已过期");
        }
    }

    private record CaptchaEntry(int answer, long expireAt) {
    }
}
