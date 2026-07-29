package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        String icon,
        String colorHex,
        boolean system
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getIcon(),
                c.getColorHex(),
                c.isSystem()
        );
    }
}

