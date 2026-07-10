package com.krypto.financeadvisor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_rules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BudgetRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    // Updated on every DEBIT transaction in this category.
    // Recalculated from scratch during month-end rollover
    // (POST /api/budgets/rollover) using REPEATABLE_READ isolation.
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal spentThisMonth = BigDecimal.ZERO;

    // Alert fires when spentThisMonth >= monthlyLimit * alertThresholdPct / 100
    @Min(1) @Max(100)
    @Column(nullable = false)
    @Builder.Default
    private int alertThresholdPct = 80;

    private LocalDateTime rolledOverAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public boolean isThresholdExceeded() {
        BigDecimal threshold = monthlyLimit
                .multiply(BigDecimal.valueOf(alertThresholdPct))
                .divide(BigDecimal.valueOf(100));
        return spentThisMonth.compareTo(threshold) >= 0;
    }
}
