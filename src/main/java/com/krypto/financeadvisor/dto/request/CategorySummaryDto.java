package com.krypto.financeadvisor.dto.request;

import java.math.BigDecimal;

public record CategorySummaryDto(String categoryName, BigDecimal total) {
}
