package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.dto.response.AuditLogResponse;
import com.krypto.financeadvisor.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    // -------------------------------------------------------
    // Returns full audit trail for the currently logged in user.
    // Ordered by createdAt DESC so most recent actions appear first.
    //
    // Interview point:
    // AuditLog.performedBy is NOT a FK to users table — intentionally.
    // If a user is deleted, their audit history must still be
    // readable for compliance. A FK would either cascade delete
    // the logs or block the user deletion entirely.
    // Storing just the userId as a plain Long avoids both problems.
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getUserAuditLogs(Long userId) {
        return auditLogRepository.findByPerformedByOrderByCreatedAtDesc(userId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    // -------------------------------------------------------
    // Returns the full change history for a specific entity.
    // e.g. GET /api/audit/entity/Account/1
    //      → shows every CREATE, UPDATE, DELETE, TRANSFER
    //        that ever touched Account with id 1
    //
    // Useful for debugging and showing users what happened
    // to a specific account or transaction over time.
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getEntityHistory(String entityType, Long entityId) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
