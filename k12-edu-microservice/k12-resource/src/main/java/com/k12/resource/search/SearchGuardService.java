package com.k12.resource.search;

import com.k12.resource.config.SearchProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 搜索合规：敏感词拦截 + 内存限流（P0+，集群环境可换 Redis）
 */
@Service
public class SearchGuardService {

    private final SearchProperties searchProperties;

    public SearchGuardService(SearchProperties searchProperties) {
        this.searchProperties = searchProperties;
    }

    private final Map<String, Deque<Long>> rateBuckets = new ConcurrentHashMap<>();

    public GuardResult checkAllowed(String clientKey, String query) {
        if (containsSensitive(query)) {
            return GuardResult.blocked("sensitive_word", "搜索内容包含不当词汇，请修改后重试");
        }
        if (isRateLimited(clientKey)) {
            return GuardResult.blocked("rate_limit", "搜索过于频繁，请稍后再试");
        }
        recordHit(clientKey);
        return GuardResult.ok();
    }

    /** suggest 仅校验敏感词，不限流 */
    public GuardResult checkSuggestAllowed(String query) {
        if (containsSensitive(query)) {
            return GuardResult.blocked("sensitive_word", "搜索内容包含不当词汇，请修改后重试");
        }
        return GuardResult.ok();
    }

    public boolean shouldHideVip(Long userId) {
        return searchProperties.isHideVipForAnonymous() && (userId == null || userId <= 0);
    }

    private boolean containsSensitive(String query) {
        if (!StringUtils.hasText(query)) {
            return false;
        }
        String lower = query.toLowerCase(Locale.ROOT);
        for (String word : searchProperties.getSensitiveWords()) {
            if (StringUtils.hasText(word) && lower.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean isRateLimited(String clientKey) {
        if (!StringUtils.hasText(clientKey)) {
            clientKey = "anonymous";
        }
        long now = System.currentTimeMillis();
        long windowMs = 60_000L;
        int limit = Math.max(searchProperties.getRateLimitPerMinute(), 1);
        Deque<Long> deque = rateBuckets.computeIfAbsent(clientKey, k -> new ConcurrentLinkedDeque<>());
        while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
            deque.pollFirst();
        }
        return deque.size() >= limit;
    }

    private void recordHit(String clientKey) {
        if (!StringUtils.hasText(clientKey)) {
            clientKey = "anonymous";
        }
        rateBuckets.computeIfAbsent(clientKey, k -> new ConcurrentLinkedDeque<>()).addLast(System.currentTimeMillis());
    }

    public record GuardResult(boolean allowed, String code, String message) {
        static GuardResult ok() {
            return new GuardResult(true, null, null);
        }

        static GuardResult blocked(String code, String message) {
            return new GuardResult(false, code, message);
        }
    }
}
