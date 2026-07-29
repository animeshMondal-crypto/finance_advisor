package com.krypto.financeadvisor.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @Positive BigDecimal monthlyLimit,
        @Min(1) @Max(100) int alertThresholdPct
) {
}
