package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.dto.request.CategorySummaryDto;

import java.math.BigDecimal;
import java.util.List;

public record TransactionSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        List<CategorySummaryDto> breakdown
) {
}
