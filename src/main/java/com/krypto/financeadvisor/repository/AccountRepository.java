package com.krypto.financeadvisor.repository;

import com.krypto.financeadvisor.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Only return active (non-soft-deleted) accounts for a user
    List<Account> findByUserIdAndActiveTrue(Long userId);

    // -------------------------------------------------------
    // OPTIMISTIC_FORCE_INCREMENT — used during fund transfers.
    // Explicitly bumps the @Version on every read, so if two
    // concurrent requests load the same account simultaneously,
    // the second save will throw OptimisticLockException.
    // This prevents the "lost update" problem without a DB lock.
    // -------------------------------------------------------
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.active = true")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    boolean existsByIdAndUserId(Long id, Long userId);
}
