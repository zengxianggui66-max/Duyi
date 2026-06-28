package com.k12.resource.search;

import com.k12.resource.config.SearchProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 搜索内存缓存（P1；集群环境可换 Redis）
 */
@Service
public class SearchCacheService {

    private final SearchProperties searchProperties;

    public SearchCacheService(SearchProperties searchProperties) {
        this.searchProperties = searchProperties;
    }

    private final Map<String, CacheEntry<?>> store = new ConcurrentHashMap<>();

    public <T> T getOrLoad(String key, int ttlSeconds, Supplier<T> loader) {
        if (!searchProperties.getCache().isEnabled() || ttlSeconds <= 0) {
            return loader.get();
        }
        long now = System.currentTimeMillis();
        CacheEntry<?> entry = store.get(key);
        if (entry != null && entry.expireAtMs > now) {
            @SuppressWarnings("unchecked")
            T cached = (T) entry.value;
            return cached;
        }
        T loaded = loader.get();
        store.put(key, new CacheEntry<>(loaded, now + ttlSeconds * 1000L));
        return loaded;
    }

    public void invalidatePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        store.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private record CacheEntry<T>(T value, long expireAtMs) {}
}
