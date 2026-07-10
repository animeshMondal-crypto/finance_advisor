package com.krypto.financeadvisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_insights", indexes = {
        @Index(name = "idx_insight_user_month", columnList = "user_id, month_year")
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AiInsight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String insightText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsightType type;   // MONTHLY_SUMMARY / ANOMALY / SUGGESTION / BUDGET_ALERT

    @Column(length = 7)
    private String monthYear;   // "2026-06" — for monthly filtering

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
