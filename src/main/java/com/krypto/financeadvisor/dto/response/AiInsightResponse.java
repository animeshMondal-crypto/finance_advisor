package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.entity.AiInsight;
import com.krypto.financeadvisor.entity.InsightType;

import java.time.LocalDateTime;

public record AiInsightResponse(
        Long id,
        String insightText,
        InsightType type,
        String monthYear,
        LocalDateTime generatedAt
) {
    public static AiInsightResponse from(AiInsight a) {
        return new AiInsightResponse(
                a.getId(),
                a.getInsightText(),
                a.getType(),
                a.getMonthYear(),
                a.getGeneratedAt()
        );
    }
}
