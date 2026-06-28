package com.k12.resource.cache;

import com.k12.resource.config.HomeProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Phase 7-F：首页 C 端读缓存（内存；集群可换 Redis）。
 */
@Service
public class HomeCacheService {

    private final HomeProperties homeProperties;

    public HomeCacheService(HomeProperties homeProperties) {
        this.homeProperties = homeProperties;
    }

    private final Map<String, CacheEntry<?>> store = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    public <T> T getOrLoad(String key, Supplier<T> loader) {
        int ttl = homeProperties.getCache().getBootstrapTtlSeconds();
        if (!homeProperties.getCache().isEnabled() || ttl <= 0) {
            missCount.incrementAndGet();
            return loader.get();
        }
        long now = System.currentTimeMillis();
        CacheEntry<?> entry = store.get(key);
        if (entry != null && entry.expireAtMs > now) {
            hitCount.incrementAndGet();
            @SuppressWarnings("unchecked")
            T cached = (T) entry.value;
            return cached;
        }
        missCount.incrementAndGet();
        T loaded = loader.get();
        store.put(key, new CacheEntry<>(loaded, now + ttl * 1000L));
        return loaded;
    }

    public void evictAll() {
        store.clear();
    }

    public void evictPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        store.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public Map<String, Object> stats() {
        return Map.of(
                "enabled", homeProperties.getCache().isEnabled(),
                "ttlSeconds", homeProperties.getCache().getBootstrapTtlSeconds(),
                "size", store.size(),
                "hits", hitCount.get(),
                "misses", missCount.get()
        );
    }

    private record CacheEntry<T>(T value, long expireAtMs) {}
}
