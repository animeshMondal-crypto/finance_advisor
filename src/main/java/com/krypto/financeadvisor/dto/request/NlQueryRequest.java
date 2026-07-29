package com.krypto.financeadvisor.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NlQueryRequest(
        @NotBlank String question
) {
}
