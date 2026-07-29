package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.dto.response.AiInsightResponse;
import com.krypto.financeadvisor.entity.AiInsight;
import com.krypto.financeadvisor.entity.InsightType;
import com.krypto.financeadvisor.repository.AiInsightRepository;
import com.krypto.financeadvisor.service.ai.NaturalLanguageQueryHandler;
import com.krypto.financeadvisor.service.interfaces.InsightGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInsightService {
    private final AiInsightRepository aiInsightRepository;
    private final List<InsightGenerator> insightGenerators; // Spring injects ALL implementations
    private final NaturalLanguageQueryHandler nlQueryHandler;

    // All insights for the user, newest first
    @Transactional(readOnly = true)
    public List<AiInsightResponse> getAllInsights(Long userId){
        return aiInsightRepository.findByUserIdOrderByGeneratedAtDesc(userId)
                .stream()
                .map(AiInsightResponse::from)
                .toList();
    }

    // Filter by month — e.g. "2026-06"
    @Transactional(readOnly = true)
    public List<AiInsightResponse> getInsightsByMonth(Long userId, String monthYear){
        return aiInsightRepository.findByUserIdAndMonthYear(userId, monthYear)
                .stream()
                .map(AiInsightResponse::from)
                .toList();
    }

    // Only return ANOMALY type insights
    @Transactional(readOnly = true)
    public List<AiInsightResponse> getAnomalies(Long userId) {
        return aiInsightRepository.findByUserIdAndType(userId, InsightType.ANOMALY)
                .stream()
                .map(AiInsightResponse::from)
                .toList();
    }

    // Only return SUGGESTION type insights
    @Transactional(readOnly = true)
    public List<AiInsightResponse> getSuggestions(Long userId) {
        return aiInsightRepository.findByUserIdAndType(userId, InsightType.SUGGESTION)
                .stream()
                .map(AiInsightResponse::from)
                .toList();
    }

    // -------------------------------------------------------
    // Placeholder — will be replaced in Phase 4 with actual
    // Spring AI call that reads transactions and generates
    // a real monthly summary using ChatClient.
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AiInsightResponse> getMonthlySummary(Long userId) {
        String currentMonth = YearMonth.now().toString(); // "2026-07"
        return aiInsightRepository
                .findByUserIdAndMonthYear(userId, currentMonth)
                .stream()
                .map(AiInsightResponse::from)
                .toList();
    }

    @Transactional
    public AiInsightResponse generateInsight(Long userId, InsightType type) {
        InsightGenerator generator = insightGenerators.stream()
                .filter(g -> g.supportedType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No generator found for insight type: " + type));

        AiInsight insight = generator.generate(userId);
        return AiInsightResponse.from(insight);
    }

    public String query(Long userId, String question) {
        return nlQueryHandler.query(userId, question);
    }
}
