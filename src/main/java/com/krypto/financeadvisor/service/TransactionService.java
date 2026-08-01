package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.dto.request.CategorySummaryDto;
import com.krypto.financeadvisor.dto.request.CreateTransactionRequest;
import com.krypto.financeadvisor.dto.response.TransactionResponse;
import com.krypto.financeadvisor.dto.response.TransactionSummaryResponse;
import com.krypto.financeadvisor.entity.Account;
import com.krypto.financeadvisor.entity.Category;
import com.krypto.financeadvisor.entity.Transaction;
import com.krypto.financeadvisor.entity.TransactionType;
import com.krypto.financeadvisor.exception.ResourceNotFountException;
import com.krypto.financeadvisor.repository.AccountRepository;
import com.krypto.financeadvisor.repository.CategoryRepository;
import com.krypto.financeadvisor.repository.TransactionRepository;
import com.krypto.financeadvisor.service.interfaces.Categorizable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;
    private final Categorizable categorizable;
    private final BudgetService budgetService;

    // -------------------------------------------------------
    // ATOMICITY DEMO
    //
    // Two things happen inside this single @Transactional:
    //   1. Transaction record is saved to DB
    //   2. Account balance is updated
    //
    // If step 2 fails (e.g. insufficient funds throws) →
    // step 1 is also rolled back. You will NEVER get a
    // transaction record without a corresponding balance change
    // or vice versa. Both happen or neither happens.
    // -------------------------------------------------------
    @Transactional
    public TransactionResponse logTransaction(Long userId, CreateTransactionRequest request){
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(()->ResourceNotFountException.of("Account", request.accountId()));

        if(account.getUser().getId()!=userId){
            throw new IllegalArgumentException("Account does not belongs to you");
        }

        // Resolve category if manually provided
        Category category = null;
        boolean categorizedByAi = false;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> ResourceNotFountException.of("Category", request.categoryId()));
        }else if (request.description() != null && !request.description().isBlank()) {
            // -------------------------------------------------------
            // AI AUTO-CATEGORIZATION
            // Only runs for DEBIT transactions with a description.
            // CREDIT transactions (salary, refunds) are often
            // best left for the user to categorize manually.
            // -------------------------------------------------------
            if (request.type() == TransactionType.DEBIT) {
                String aiCategory = categorizable.categorize(
                        request.description(),
                        request.amount().toString()
                );

                // Find the matching system category by name
                category = categoryRepository.findAllVisibleToUser(userId)
                        .stream()
                        .filter(c -> c.getName().equalsIgnoreCase(aiCategory))
                        .findFirst()
                        .orElse(null);

                log.info("this is the category {}", category);

                categorizedByAi = category != null;
            }
        }


        // Update account balance based on transaction type
        // debit() throws InsufficientFundsException if balance too low
        // which triggers a full rollback of this entire method
        if (request.type() == TransactionType.DEBIT) {
            account.debit(request.amount());
        } else if (request.type() == TransactionType.CREDIT) {
            account.credit(request.amount());
        }
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .amount(request.amount())
                .type(request.type())
                .description(request.description())
                .account(account)
                .category(category)
                .aiCategoryRaw(category != null ? category.getName() : "Other")
                .categorizedByAi(categorizedByAi)
                .occurredAt(request.occurredAt() != null
                        ? request.occurredAt()
                        : LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        if (saved.getType() == TransactionType.DEBIT && category != null) {
            budgetService.updateSpendForCategory(
                    userId,
                    category.getId(),
                    saved.getAmount()
            );
        }


        auditLogService.log("CREATE_TRANSACTION", "Transaction", saved.getId(), userId,
                Map.of("amount", saved.getAmount(), "type", saved.getType().name(),
                        "categorizedByAi", categorizedByAi));

        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(
            Long userId, LocalDateTime from, LocalDateTime to) {

        // Default to current month if no date range provided
        LocalDateTime start = from != null ? from : YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime end = to != null ? to : LocalDateTime.now();

        return transactionRepository.findByUserAndDateRange(userId, start, end)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long userId, Long transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> ResourceNotFountException.of("Transaction", transactionId));

        if (tx.getAccount().getUser().getId()!=userId) {
            throw new ResourceNotFountException("Transaction not found with id: " + transactionId);
        }

        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse overrideCategory(Long userId, Long transactionId, Long categoryId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> ResourceNotFountException.of("Transaction", transactionId));

        if (tx.getAccount().getUser().getId()!=userId) {
            throw new IllegalArgumentException("Transaction does not belong to you");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFountException.of("Category", categoryId));

        tx.setCategory(category);
        tx.setCategorizedByAi(false);  // mark as manually overridden

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    // -------------------------------------------------------
    // ROLLBACK DEMO
    //
    // Deleting a transaction must also reverse the balance.
    // Both operations are inside one @Transactional so:
    //
    //   - If balance reversal throws → transaction record
    //     is NOT deleted. Data stays consistent.
    //   - If delete throws → balance is NOT changed.
    //     Again, consistent.
    //
    // This is the same atomicity guarantee as logTransaction
    // but in reverse — a good interview talking point.
    // -------------------------------------------------------
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> ResourceNotFountException.of("Transaction", transactionId));

        Account account = tx.getAccount();

        if (account.getUser().getId()!=userId) {
            throw new IllegalArgumentException("Transaction does not belong to you");
        }

        // Reverse the balance effect of this transaction
        if (tx.getType() == TransactionType.DEBIT) {
            account.credit(tx.getAmount());   // undo the debit
        } else if (tx.getType() == TransactionType.CREDIT) {
            account.debit(tx.getAmount());    // undo the credit
        }

        accountRepository.save(account);
        transactionRepository.delete(tx);

        auditLogService.log("DELETE_TRANSACTION", "Transaction", transactionId, userId,
                Map.of("amount", tx.getAmount(), "type", tx.getType().name()));

        log.info("Transaction {} deleted, balance of account {} reversed", transactionId, account.getId());
    }

    // -------------------------------------------------------
    // Summary — used for dashboard and AI context building
    // Calculates total income, total expenses, net balance
    // and a per-category breakdown for the given date range
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getSummary(Long userId, LocalDateTime from, LocalDateTime to) {
        LocalDateTime start = from != null ? from : YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime end = to != null ? to : LocalDateTime.now();

        List<Transaction> transactions = transactionRepository
                .findByUserAndDateRange(userId, start, end);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategorySummaryDto> breakdown = transactionRepository
                .getCategoryBreakdown(userId, start, end);

        return new TransactionSummaryResponse(
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses),
                breakdown
        );
    }

}
