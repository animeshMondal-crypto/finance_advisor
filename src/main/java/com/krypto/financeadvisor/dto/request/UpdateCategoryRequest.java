package com.krypto.financeadvisor.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(@NotNull Long categoryId) {
}
