package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.dto.request.CreateBudgetRequest;
import com.krypto.financeadvisor.dto.request.UpdateBudgetRequest;
import com.krypto.financeadvisor.dto.response.BudgetResponse;
import com.krypto.financeadvisor.entity.BudgetRule;
import com.krypto.financeadvisor.entity.Category;
import com.krypto.financeadvisor.entity.User;
import com.krypto.financeadvisor.exception.ResourceNotFountException;
import com.krypto.financeadvisor.repository.BudgetRuleRepository;
import com.krypto.financeadvisor.repository.CategoryRepository;
import com.krypto.financeadvisor.repository.TransactionRepository;
import com.krypto.financeadvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.krypto.financeadvisor.kafka.event.BudgetAlertEvent;
import com.krypto.financeadvisor.kafka.event.BudgetAlertType;
import com.krypto.financeadvisor.kafka.producer.BudgetAlertProducer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {
    private final BudgetRuleRepository budgetRuleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;
    private final BudgetAlertProducer budgetAlertProducer;

    @CacheEvict(
            value = "budgets",
            key = "#userId"
    )
    @Transactional
    public BudgetResponse createBudget(Long userId, CreateBudgetRequest request) {
        if (budgetRuleRepository.existsByUserIdAndCategoryId(userId, request.categoryId())) {
            throw new IllegalArgumentException(
                    "A budget rule already exists for this category"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFountException.of("User", userId));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFountException.of("Category", request.categoryId()));

        //calculate current month spend
        BigDecimal currentSpend = getCurrentMonthSpend(userId, request.categoryId());

        BudgetRule budget = BudgetRule.builder()
                .user(user)
                .category(category)
                .monthlyLimit(request.monthlyLimit())
                .spentThisMonth(currentSpend)
                .alertThresholdPct(request.alertThresholdPct() > 0 ? request.alertThresholdPct() : 80)
                .build();

        BudgetRule saved = budgetRuleRepository.save(budget);

        auditLogService.log("CREATE_BUDGET", "BudgetRule", saved.getId(), userId,
                Map.of("category", category.getName(),
                        "limit", request.monthlyLimit()));

        return BudgetResponse.from(saved);
    }

    @Cacheable(
            value = "budgets",
            key = "#userId"
    )
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets(Long userId) {
        return budgetRuleRepository.findByUserId(userId)
                .stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Cacheable(
            value = "budgets",
            key = "#userId + '-budget-' + #budgetId"
    )
    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long userId, Long budgetId) {
        return BudgetResponse.from(getOwnedBudget(userId, budgetId));
    }

    @Caching(evict = {
            @CacheEvict(value = "budgets", key = "#userId"),
            @CacheEvict(value = "budgets", key = "#userId + '-budget-' + #budgetId")
    })
    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, UpdateBudgetRequest request) {
        BudgetRule budget = getOwnedBudget(userId, budgetId);

        if (request.monthlyLimit() != null) {
            budget.setMonthlyLimit(request.monthlyLimit());
        }
        if (request.alertThresholdPct() > 0) {
            budget.setAlertThresholdPct(request.alertThresholdPct());
        }

        return BudgetResponse.from(budgetRuleRepository.save(budget));
    }

    @Caching(evict = {
            @CacheEvict(value = "budgets", key = "#userId"),
            @CacheEvict(value = "budgets", key = "#userId + '-budget-' + #budgetId")
    })
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        BudgetRule budget = getOwnedBudget(userId, budgetId);
        budgetRuleRepository.delete(budget);

        auditLogService.log("DELETE_BUDGET", "BudgetRule", budgetId, userId,
                Map.of("category", budget.getCategory().getName()));
    }

    // -------------------------------------------------------
    // ROLLOVER — the most important method in this service.
    //
    // Isolation.REPEATABLE_READ — the key interview concept here:
    //
    // Default isolation (READ_COMMITTED) means another transaction
    // could INSERT a new transaction for this user's category
    // WHILE we are looping through budgets and summing spend.
    // This would cause budget 1 to be calculated with the new
    // transaction included, but budget 2 to not see it yet —
    // inconsistent snapshot across the same batch operation.
    //
    // REPEATABLE_READ guarantees that every SELECT in this
    // transaction sees the SAME snapshot of data — the one
    // that existed when this transaction started.
    // No new transactions inserted by other users mid-loop
    // will affect our calculations.
    //
    // This is critical for month-end batch jobs where accuracy
    // across all rows must be consistent.
    // -------------------------------------------------------
    @Caching(evict = {
            @CacheEvict(value = "budgets",    allEntries = true),
            @CacheEvict(value = "tx-summary", key = "#userId")
    })
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<BudgetResponse> rolloverBudgets(Long userId) {
        List<BudgetRule> budgets = budgetRuleRepository.findByUserId(userId);

        if (budgets.isEmpty()) {
            throw new IllegalArgumentException("No budget rules found for this user");
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDateTime.now();

        // -------------------------------------------------------
        // Each iteration reads transaction data for one category.
        // With REPEATABLE_READ, all these reads see the same
        // consistent snapshot — no mid-loop dirty data.
        // -------------------------------------------------------
        for (BudgetRule budget : budgets) {
            BigDecimal actualSpend = transactionRepository.sumDebitByUserAndCategory(
                    userId,
                    budget.getCategory().getId(),
                    monthStart,
                    monthEnd
            );

            BigDecimal previousSpend = budget.getSpentThisMonth();
            budget.setSpentThisMonth(actualSpend);
            budget.setRolledOverAt(LocalDateTime.now());

            log.info("Budget rollover — category: {}, previous spend: {}, recalculated: {}",
                    budget.getCategory().getName(), previousSpend, actualSpend);

            //Fire alert if threshold crossed after recalculation
            if (budget.isThresholdExceeded()) {
                log.warn("Budget alert — category '{}' has reached {}% of ₹{} limit. Spent: ₹{}",
                        budget.getCategory().getName(),
                        budget.getAlertThresholdPct(),
                        budget.getMonthlyLimit(),
                        actualSpend);
                // TODO Phase 4: trigger notification via AI insight generation
            }
        }

        List<BudgetRule> saved = budgetRuleRepository.saveAll(budgets);

        auditLogService.log("ROLLOVER_BUDGETS", "BudgetRule", null, userId,
                Map.of("month", currentMonth.toString(),
                        "budgetsProcessed", budgets.size()));

        return saved.stream().map(BudgetResponse::from).toList();
    }

    // -------------------------------------------------------
    // Called by TransactionService in Phase 4 after every
    // DEBIT transaction to keep spentThisMonth up to date
    // in real time, without waiting for manual rollover.
    // -------------------------------------------------------
    @CacheEvict(value = "budgets", key = "#userId")
    @Transactional
    public void updateSpendForCategory(Long userId, Long categoryId, BigDecimal amount) {
        budgetRuleRepository.findByUserIdAndCategoryId(userId, categoryId)
                .ifPresent(budget -> {
                    BigDecimal previousSpent = budget.getSpentThisMonth();
                    BigDecimal updatedSpent = previousSpent.add(amount);

                    budget.setSpentThisMonth(updatedSpent);

                    if (budget.isThresholdExceeded()) {
                        log.warn("Budget threshold exceeded for category: {}",
                                budget.getCategory().getName());
                    }

                    budgetRuleRepository.save(budget);

                    publishAlertsForNewCrossings(budget, previousSpent, updatedSpent);
                });
    }

    private BudgetRule getOwnedBudget(Long userId, Long budgetId) {
        BudgetRule budget = budgetRuleRepository.findById(budgetId)
                .orElseThrow(() -> ResourceNotFountException.of("Budget", budgetId));

        if (budget.getUser().getId() != userId) {
            throw new IllegalArgumentException("Budget does not belong to you");
        }

        return budget;
    }

    private BigDecimal getCurrentMonthSpend(Long userId, Long categoryId) {
        YearMonth currentMonth = YearMonth.now();

        return transactionRepository.sumDebitByUserAndCategory(
                userId,
                categoryId,
                currentMonth.atDay(1).atStartOfDay(),
                LocalDateTime.now()
        );
    }

    private void publishAlertsForNewCrossings(
            BudgetRule budget,
            BigDecimal previousSpent,
            BigDecimal currentSpent
    ){
        BigDecimal warningThreshold = budget.getMonthlyLimit()
                .multiply(BigDecimal.valueOf(budget.getAlertThresholdPct()))
                .divide(BigDecimal.valueOf(100));


        if(budget.getAlertThresholdPct() < 100 && crossed(previousSpent, currentSpent, warningThreshold)){
            publishBudgetAlert(
                    budget,
                    currentSpent,
                    budget.getAlertThresholdPct(),
                    BudgetAlertType.THRESHOLD_REACHED
            );
        }

        if (crossed(previousSpent, currentSpent, budget.getMonthlyLimit())) {
            publishBudgetAlert(
                    budget,
                    currentSpent,
                    100,
                    BudgetAlertType.BUDGET_EXCEEDED
            );
        }
    }

    private boolean crossed(BigDecimal previousSpend,
                            BigDecimal currentSpend,
                            BigDecimal threshold){
        return previousSpend.compareTo(threshold) < 0 && currentSpend.compareTo(threshold) >=0;
    }

    private void publishBudgetAlert(BudgetRule budget,
                                    BigDecimal currentSpend,
                                    int thresholdPct,
                                    BudgetAlertType alertType){
        BudgetAlertEvent event = BudgetAlertEvent
                .builder()
                .userId(budget.getUser().getId())
                .budgetRuleId(budget.getId())
                .categoryName(budget.getCategory().getName())
                .monthlyLimit(budget.getMonthlyLimit())
                .spentThisMonth(currentSpend)
                .alertThresholdPct(thresholdPct)
                .alertType(alertType)
                .budgetMonth(YearMonth.now().toString())
                .triggeredAt(LocalDateTime.now())
                .build();

        budgetAlertProducer.publish(event);

    }
}
