package com.krypto.financeadvisor.dto.request;

import com.krypto.financeadvisor.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTransactionRequest(@NotNull Long accountId,
                                       @NotNull @Positive BigDecimal amount,
                                       @NotNull TransactionType type,
                                       String description,
                                       Long categoryId,          // optional — if null, AI will categorize in Phase 4
                                       LocalDateTime occurredAt)  // optional — defaults to now if not provided
{
}
