package com.krypto.financeadvisor.repository;

import com.krypto.financeadvisor.entity.BudgetRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRuleRepository extends JpaRepository<BudgetRule, Long> {
    List<BudgetRule> findByUserId(Long userId);

    Optional<BudgetRule> findByUserIdAndCategoryId(Long userId, Long categoryId);

    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);
}
