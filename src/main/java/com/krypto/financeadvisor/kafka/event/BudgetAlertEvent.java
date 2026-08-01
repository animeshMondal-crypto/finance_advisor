package com.krypto.financeadvisor.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertEvent {
    private Long userId;
    private Long budgetRuleId;
    private String categoryName;
    private BigDecimal monthlyLimit;
    private BigDecimal spentThisMonth;
    private int alertThresholdPct;
    private BudgetAlertType alertType;        // "THRESHOLD_EXCEEDED" or "LIMIT_EXCEEDED"
    private String budgetMonth;
    private LocalDateTime triggeredAt;
}
