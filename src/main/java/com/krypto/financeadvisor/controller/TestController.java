package com.krypto.financeadvisor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final CacheManager cacheManager;

    @GetMapping("/ping")
    public String ping() {
        return "pong — you are authenticated!";
    }

    @GetMapping("/cache-status")
    public Map<String, Object> cacheStatus(){
        Map<String, Object> status = new LinkedHashMap<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);

            if (cache instanceof CaffeineCache caffeineCache) {
                var nativeCache = caffeineCache.getNativeCache();
                var stats = nativeCache.stats();

                status.put(cacheName, Map.of(
                        "estimatedSize", nativeCache.estimatedSize(),
                        "hits", stats.hitCount(),
                        "misses", stats.missCount(),
                        "hitRate", stats.hitRate()
                ));
            }
        }

        return status;
    }
}
