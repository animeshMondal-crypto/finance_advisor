package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.entity.BudgetRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(
        Long id,
        String categoryName,
        String categoryIcon,
        BigDecimal monthlyLimit,
        BigDecimal spentThisMonth,
        BigDecimal remainingBalance,
        int alertThresholdPct,
        boolean thresholdExceeded,
        boolean limitExceeded,
        LocalDateTime rolledOverAt
) {
    public static BudgetResponse from(BudgetRule b) {
        return new BudgetResponse(
                b.getId(),
                b.getCategory().getName(),
                b.getCategory().getIcon(),
                b.getMonthlyLimit(),
                b.getSpentThisMonth(),
                b.getMonthlyLimit().subtract(b.getSpentThisMonth()),
                b.getAlertThresholdPct(),
                b.isThresholdExceeded(),
                b.getSpentThisMonth().compareTo(b.getMonthlyLimit()) > 0,
                b.getRolledOverAt()
        );
    }
}
