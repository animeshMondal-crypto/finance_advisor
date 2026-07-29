package com.krypto.financeadvisor.controller;

import com.krypto.financeadvisor.dto.request.NlQueryRequest;
import com.krypto.financeadvisor.dto.response.AiInsightResponse;
import com.krypto.financeadvisor.dto.response.ApiResponse;
import com.krypto.financeadvisor.dto.response.NlQueryResponse;
import com.krypto.financeadvisor.entity.InsightType;
import com.krypto.financeadvisor.service.AiInsightService;
import com.krypto.financeadvisor.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class AiInsightController {
    private final AiInsightService aiInsightService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiInsightResponse>>> getAllInsights() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                aiInsightService.getAllInsights(userId)));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<List<AiInsightResponse>>> getAnomalies() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                aiInsightService.getAnomalies(userId)));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<AiInsightResponse>>> getSuggestions() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                aiInsightService.getSuggestions(userId)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<AiInsightResponse>>> getMonthlySummary() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                aiInsightService.getMonthlySummary(userId)));
    }

    @GetMapping("/month/{monthYear}")
    public ResponseEntity<ApiResponse<List<AiInsightResponse>>> getByMonth(
            @PathVariable String monthYear) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                aiInsightService.getInsightsByMonth(userId, monthYear)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AiInsightResponse>> generateInsight(
            @RequestParam InsightType type) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok("Insight generated",
                aiInsightService.generateInsight(userId, type)));
    }

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<NlQueryResponse>> query(
            @Valid @RequestBody NlQueryRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        String answer = aiInsightService.query(userId, request.question());
        return ResponseEntity.ok(ApiResponse.ok(
                new NlQueryResponse(request.question(), answer)));
    }
}
