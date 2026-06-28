package com.k12.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类
 */
@Component
public class JwtUtils {

    /** 与各服务 application.yml 中 jwt.secret 保持一致；缺省值避免未打包配置时启动失败 */
    @Value("${jwt.secret:k12-education-platform-secret-key-2026-spring-boot-jwt-token}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String username, Map<String, Object> claims) {
        Map<String, Object> tokenClaims = new java.util.HashMap<>();
        if (claims != null) {
            tokenClaims.putAll(claims);
        }
        tokenClaims.putIfAbsent("jti", UUID.randomUUID().toString().replace("-", ""));
        return Jwts.builder()
                .claims(tokenClaims)
                .subject(username)
                .id(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String getJti(String token) {
        Object jti = parseToken(token).get("jti");
        return jti != null ? String.valueOf(jti) : null;
    }

    public boolean isTokenValidAndNotBlacklisted(String token,
                                                  com.k12.common.auth.TokenBlacklistService blacklistService) {
        if (!validateToken(token)) {
            return false;
        }
        if (blacklistService == null) {
            return true;
        }
        String jti = getJti(token);
        return !blacklistService.isBlacklisted(jti);
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getId());
    }

    /**
     * 获取用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
