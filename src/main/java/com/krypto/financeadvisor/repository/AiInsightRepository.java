package com.krypto.financeadvisor.repository;

import com.krypto.financeadvisor.entity.AiInsight;
import com.krypto.financeadvisor.entity.InsightType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {
    List<AiInsight> findByUserIdOrderByGeneratedAtDesc(Long userId);

    List<AiInsight> findByUserIdAndMonthYear(Long userId, String monthYear);

    List<AiInsight> findByUserIdAndType(Long userId, InsightType type);

    // Prevents generating duplicate monthly summaries
    boolean existsByUserIdAndMonthYearAndType(Long userId, String monthYear, InsightType type);
}
