package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.entity.Transaction;
import com.krypto.financeadvisor.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        String categoryName,
        boolean categorizedByAi,
        Long accountId,
        String accountName,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction t){
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType(),
                t.getDescription(),
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.isCategorizedByAi(),
                t.getAccount().getId(),
                t.getAccount().getName(),
                t.getOccurredAt(),
                t.getCreatedAt()
        );
    }
}
