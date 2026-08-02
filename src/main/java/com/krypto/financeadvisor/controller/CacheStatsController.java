package com.krypto.financeadvisor.controller;

import com.krypto.financeadvisor.dto.response.ApiResponse;
import com.krypto.financeadvisor.dto.response.CacheStatsResponse;
import com.krypto.financeadvisor.service.CacheStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheStatsController {
    private final CacheStatsService cacheStatsService;

    // GET /api/cache/stats — all caches
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<CacheStatsResponse>>> getAllStats() {
        return ResponseEntity.ok(ApiResponse.ok(cacheStatsService.getAllStats()));
    }

    // GET /api/cache/stats/accounts — specific cache
    @GetMapping("/stats/{cacheName}")
    public ResponseEntity<ApiResponse<CacheStatsResponse>> getStatsByName(
            @PathVariable String cacheName) {
        return ResponseEntity.ok(ApiResponse.ok(
                cacheStatsService.getStatsByName(cacheName)));
    }

    // DELETE /api/cache/evict — wipe all caches manually
    @DeleteMapping("/evict")
    public ResponseEntity<ApiResponse<Void>> evictAll() {
        cacheStatsService.evictAll();
        return ResponseEntity.ok(ApiResponse.ok("All caches evicted", null));
    }

    // DELETE /api/cache/evict/accounts — wipe one specific cache
    @DeleteMapping("/evict/{cacheName}")
    public ResponseEntity<ApiResponse<Void>> evictByName(
            @PathVariable String cacheName) {
        cacheStatsService.evictByName(cacheName);
        return ResponseEntity.ok(ApiResponse.ok(
                "Cache '" + cacheName + "' evicted", null));
    }
}
