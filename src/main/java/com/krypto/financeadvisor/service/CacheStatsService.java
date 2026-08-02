package com.krypto.financeadvisor.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.krypto.financeadvisor.dto.response.CacheStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheStatsService {
    private final CacheManager cacheManager;

    // -------------------------------------------------------
    // CacheManager holds all named caches we registered in
    // CacheConfig. We cast each one to CaffeineCache to access
    // Caffeine-specific stats like hitCount, missCount etc.
    //
    // recordStats() must be enabled in CacheConfig on each
    // Caffeine builder — we already did this.
    // -------------------------------------------------------

    public List<CacheStatsResponse> getAllStats() {
        return cacheManager.getCacheNames()
                .stream()
                .map(this::buildStats)
                .toList();
    }

    public CacheStatsResponse getStatsByName(String cacheName) {
        if (!cacheManager.getCacheNames().contains(cacheName)) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }
        return buildStats(cacheName);
    }

    public void evictAll() {
        cacheManager.getCacheNames()
                .forEach(name -> {
                    var cache = cacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                        log.info("Cache '{}' manually evicted", name);
                    }
                });
    }

    public void evictByName(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }
        cache.clear();
        log.info("Cache '{}' manually evicted", cacheName);
    }

    // --- Internal helper ---

    private CacheStatsResponse buildStats(String cacheName) {
        var springCache = cacheManager.getCache(cacheName);

        if (!(springCache instanceof CaffeineCache caffeineCache)) {
            return new CacheStatsResponse(cacheName, 0, 0, 0.0, 0, 0);
        }

        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        CacheStats stats = nativeCache.stats();

        return new CacheStatsResponse(
                cacheName,
                stats.hitCount(),
                stats.missCount(),
                // round to 2 decimal places for readability
                Math.round(stats.hitRate() * 10000.0) / 100.0,
                stats.evictionCount(),
                nativeCache.estimatedSize()
        );
    }
}
