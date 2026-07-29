package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.entity.Account;
import com.krypto.financeadvisor.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(Long id,
                              String name,
                              AccountType type,
                              BigDecimal balance,
                              String currency,
                              LocalDateTime createdAt) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getName(),
                a.getType(),
                a.getBalance(),
                a.getCurrency(),
                a.getCreatedAt()
        );
    }
}
