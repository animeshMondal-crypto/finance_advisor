package com.krypto.financeadvisor.dto.response;

import com.krypto.financeadvisor.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogResponse(
        Long id,
        String action,
        String entityType,
        Long entityId,
        Long performedBy,
        Map<String, Object> details,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getPerformedBy(),
                a.getDetails(),
                a.getCreatedAt()
        );
    }
}
