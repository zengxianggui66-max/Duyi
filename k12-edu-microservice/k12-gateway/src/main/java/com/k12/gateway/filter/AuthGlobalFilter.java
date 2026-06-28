package com.k12.gateway.filter;

import com.k12.common.auth.TokenBlacklistService;
import com.k12.common.util.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局 JWT 鉴权过滤器
 * 网关层统一验证 Token，将用户信息写入请求头透传给下游微服务
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 白名单路径（不需要登录） */
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/captcha",
            "/api/auth/logout",
            "/api/auth/sms/**",
            "/api/auth/oauth/url",
            "/api/auth/oauth/login",
            "/api/auth/oauth/complete",
            "/api/public/feature-flags",
            "/api/news/**",
            "/api/resource/list",
            "/api/resource/hot",
            "/api/resource/recommend",
            "/api/resource/detail/**",
            "/api/resource/search",
            "/api/resource/stats",
            "/api/primary-chinese/page",
            "/api/primary-chinese/list",
            "/api/primary-chinese/filter-options",
            "/api/primary-chinese/upload-filter-options",
            "/api/primary-chinese/unit-names",
            "/api/primary-chinese/grade-names",
            "/api/primary-chinese/editions",
            "/api/primary-chinese/modules",
            "/api/primary-chinese/types",
            "/api/primary-chinese/unit-tree",
            "/api/primary-chinese/suites",
            "/api/primary-chinese/module-stats",
            "/api/brands",
            "/api/brands/**",
            "/api/catalog/schemes",
            "/api/catalog/tree",
            "/api/catalog/node/*/breadcrumb",
            "/api/taxonomy/**",
            "/api/dictionary/**",
            "/api/resources/browse",
            "/api/resources/browse/**",
            "/api/resources/page",
            "/api/resources/detail/**",
            "/api/resources/resolve-global-id",
            "/api/resources/stats",
            "/api/resources/types",
            "/api/resources/suites",
            "/api/resources/module-stats",
            "/api/edu-resource/**",
            "/api/home/**",
            "/api/channel/**",
            "/api/culture/filter-options",
            "/api/culture/resources/page",
            "/api/culture/packages/page",
            "/api/competition/filter-options",
            "/api/competition/resources/page",
            "/api/competition/resources/*",
            "/api/competition/resources/*/view",
            "/api/competition/resources/*/download",
            "/api/competition/packages/page",
            "/api/competition/packages/*",
            "/api/competition/packages/*/download",
            "/api/topic/filter-options",
            "/api/topic/stats",
            "/api/topic/calendar-hint",
            "/api/topic/hot-keywords",
            "/api/topic/resources/hot",
            "/api/topic/resources/latest",
            "/api/topic/resources/page",
            "/api/topic/resources/*",
            "/api/topic/resources/*/view",
            "/api/topic/resources/*/download",
            "/api/topic/albums/page",
            "/api/topic/albums/*",
            "/api/topic/albums/*/download",
            "/api/file/formats",
            "/api/file/check-format",
            "/api/file/preview",
            "/api/file/preview-info",
            "/api/search/hot-keywords",
            "/api/search/all",
            "/api/search/suggest",
            "/api/search/redirect",
            "/api/search/click",
            "/api/prep/questions/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Phase 9-A：旧 /api/search/admin/** 已迁移，统一 404（含匿名请求）
        if (PATH_MATCHER.match("/api/search/admin/**", path)) {
            return notFoundResponse(exchange.getResponse());
        }

        // 白名单放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 获取 Token
        String token = resolveToken(exchange.getRequest());
        if (!StringUtils.hasText(token)
                || !jwtUtils.isTokenValidAndNotBlacklisted(token, tokenBlacklistService)) {
            return unauthorizedResponse(exchange.getResponse(), "未登录或Token已过期");
        }

        try {
            // 解析用户信息
            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);
            String role = String.valueOf(jwtUtils.parseToken(token).get("role"));

            // 将用户信息写入请求头，透传给下游微服务
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            return unauthorizedResponse(exchange.getResponse(), "Token解析失败");
        }
    }

    private boolean isWhiteListed(String path) {
        if (WHITE_LIST.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path))) {
            return true;
        }
        // 详情与计数（仅数字 ID，避免放行 /save 等写接口）
        if (path.matches("/api/primary-chinese/\\d+")) {
            return true;
        }
        if (path.matches("/api/primary-chinese/\\d+/(view|download|file)")) {
            return true;
        }
        if (path.matches("/api/culture/resources/\\d+")) {
            return true;
        }
        if (path.matches("/api/culture/resources/\\d+/(view|download)")) {
            return true;
        }
        if (path.matches("/api/culture/packages/\\d+")) {
            return true;
        }
        if (path.matches("/api/culture/packages/\\d+/download")) {
            return true;
        }
        if (path.matches("/api/competition/resources/\\d+")) {
            return true;
        }
        if (path.matches("/api/competition/resources/\\d+/(view|download)")) {
            return true;
        }
        if (path.matches("/api/competition/packages/\\d+")) {
            return true;
        }
        if (path.matches("/api/competition/packages/\\d+/download")) {
            return true;
        }
        if (path.matches("/api/topic/resources/\\d+")) {
            return true;
        }
        if (path.matches("/api/topic/resources/\\d+/(view|download)")) {
            return true;
        }
        if (path.matches("/api/topic/albums/\\d+")) {
            return true;
        }
        if (path.matches("/api/resources/\\d+/preview")) {
            return true;
        }
        if (path.matches("/api/resources/\\d+/(view|download|collect)")) {
            return true;
        }
        return path.matches("/api/topic/albums/\\d+/download");
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearer = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> notFoundResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":404,\"message\":\"搜索管理 API 已迁移至 /api/admin/search/*\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // 最高优先级
    }
}
