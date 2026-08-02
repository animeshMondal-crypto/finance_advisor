package com.krypto.financeadvisor.dto.response;

public record CacheStatsResponse(
        String  cacheName,
        long    hitCount,
        long    missCount,
        double  hitRate,
        long    evictionCount,
        long    estimatedSize
) {
}
