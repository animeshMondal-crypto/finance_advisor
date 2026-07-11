package com.krypto.financeadvisor.repository;

import com.krypto.financeadvisor.dto.request.CategorySummaryDto;
import com.krypto.financeadvisor.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // All transactions for a user across all their accounts within a date range
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.user.id = :userId
              AND t.occurredAt BETWEEN :from AND :to
            ORDER BY t.occurredAt DESC
            """)
    List<Transaction> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // -------------------------------------------------------
    // Used by BudgetService to track current month spend
    // per category. Only sums DEBIT transactions.
    // -------------------------------------------------------
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.account.user.id = :userId
              AND t.category.id = :categoryId
              AND t.type = 'DEBIT'
              AND t.occurredAt BETWEEN :from AND :to
            """)
    BigDecimal sumDebitByUserAndCategory(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // -------------------------------------------------------
    // Constructor expression — maps query result directly
    // into a DTO without needing a full entity load.
    // Used for monthly summary and AI context building.
    // -------------------------------------------------------
    @Query("""
            SELECT new com.krypto.financeadvisor.dto.response.CategorySummaryDto(
                c.name, SUM(t.amount)
            )
            FROM Transaction t
            JOIN t.category c
            WHERE t.account.user.id = :userId
              AND t.type = 'DEBIT'
              AND t.occurredAt BETWEEN :from AND :to
            GROUP BY c.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategorySummaryDto> getCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Links both legs of a transfer transaction
    List<Transaction> findByTransferPairId(Long transferPairId);
}
