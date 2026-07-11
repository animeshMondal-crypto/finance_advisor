package com.krypto.financeadvisor.dto.request;

import com.krypto.financeadvisor.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String name,
        @NotNull AccountType type,
        @PositiveOrZero BigDecimal initialBalance,
        String currency
) {}