package com.krypto.financeadvisor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_account_id",   columnList = "account_id"),
        @Index(name = "idx_tx_category_id",  columnList = "category_id"),
        @Index(name = "idx_tx_occurred_at",  columnList = "occurred_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;   // DEBIT / CREDIT / TRANSFER

    @Column(length = 255)
    private String description;

    // Stores raw AI response for debugging / audit
    @Column(columnDefinition = "TEXT")
    private String aiCategoryRaw;

    // false if user manually overrode the AI-assigned category
    @Column(nullable = false)
    @Builder.Default
    private boolean categorizedByAi = false;

    // Links both legs of a TRANSFER (debit + credit row share the same value)
    private Long transferPairId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")   // nullable — AI may not always resolve
    private Category category;
}
