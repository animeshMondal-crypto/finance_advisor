package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.entity.AuditLog;
import com.krypto.financeadvisor.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    // -------------------------------------------------------
    // Propagation.REQUIRES_NEW — runs in its OWN transaction,
    // completely separate from the caller's transaction.
    //
    // Real world example:
    //   A fund transfer fails halfway → parent TX rolls back
    //   → account balances are restored.
    //   BUT the audit log still records the ATTEMPT because
    //   it committed in its own independent transaction.
    //
    // Without REQUIRES_NEW: audit log would roll back with
    // the transfer and the attempt would be invisible.
    // -------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId,
                    Long performedBy, Map<String, Object> details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }
}
