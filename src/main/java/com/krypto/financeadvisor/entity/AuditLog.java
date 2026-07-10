package com.krypto.financeadvisor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity",   columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_user",     columnList = "performed_by")
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String action;          // "TRANSFER", "DELETE_TRANSACTION", etc.

    @Column(nullable = false, length = 50)
    private String entityType;      // "Account", "Transaction", etc.

    private Long entityId;

    // Not a FK — audit logs must survive user deletion
    private Long performedBy;

    // JSONB stores arbitrary details per action
    // e.g. { "fromAccount": 1, "toAccount": 2, "amount": 5000 }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
