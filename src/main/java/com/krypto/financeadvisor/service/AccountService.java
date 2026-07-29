package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.dto.request.CreateAccountRequest;
import com.krypto.financeadvisor.dto.request.TransferRequest;
import com.krypto.financeadvisor.dto.response.AccountResponse;
import com.krypto.financeadvisor.entity.Account;
import com.krypto.financeadvisor.entity.User;
import com.krypto.financeadvisor.exception.InsufficientFundsException;
import com.krypto.financeadvisor.exception.ResourceNotFountException;
import com.krypto.financeadvisor.repository.AccountRepository;
import com.krypto.financeadvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFountException.of("User", userId));

        Account account = Account.builder()
                .name(request.name())
                .type(request.type())
                .balance(request.initialBalance() != null
                        ? request.initialBalance()
                        : BigDecimal.ZERO)
                .currency(request.currency() != null
                        ? request.currency()
                        : user.getCurrency())
                .user(user)
                .build();

        Account saved = accountRepository.save(account);

        auditLogService.log("CREATE_ACCOUNT", "Account", saved.getId(), userId,
                Map.of("name", saved.getName(), "type", saved.getType().name()));

        return AccountResponse.from(saved);
    }

    // readOnly = true — Hibernate skips dirty checking on all entities
    // loaded in this transaction, giving a small performance boost
    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts(Long userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long userId, Long accountId) {
        return AccountResponse.from(getOwnedAccount(userId, accountId));
    }

    @Transactional
    public AccountResponse updateAccount(Long userId, Long accountId, String name) {
        Account account = getOwnedAccount(userId, accountId);
        account.setName(name);
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        Account account = getOwnedAccount(userId, accountId);
        account.setActive(false);    // soft delete — data is preserved
        accountRepository.save(account);

        auditLogService.log("DELETE_ACCOUNT", "Account", accountId, userId,
                Map.of("name", account.getName()));
    }

    // -------------------------------------------------------
    // TRANSFER — demonstrates 3 transaction concepts at once:
    //
    // 1. ATOMICITY (@Transactional)
    //    Both debit and credit happen together or not at all.
    //    If credit throws after debit succeeds → full rollback.
    //    No money is lost or created.
    //
    // 2. OPTIMISTIC LOCKING (@Version + findByIdForUpdate)
    //    Both accounts are loaded with OPTIMISTIC_FORCE_INCREMENT.
    //    If another request modified either account between our
    //    read and our save → ObjectOptimisticLockingFailureException
    //    → GlobalExceptionHandler returns 409 Conflict.
    //
    // 3. PROPAGATION (AuditLogService.REQUIRES_NEW)
    //    Audit log commits in its own transaction.
    //    Even if the transfer rolls back, the audit entry survives.
    // -------------------------------------------------------
    @Transactional
    public void transfer(Long userId, TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account from = accountRepository.findByIdForUpdate(request.fromAccountId())
                .orElseThrow(() -> ResourceNotFountException.of("Source account", request.fromAccountId()));

        Account to = accountRepository.findByIdForUpdate(request.toAccountId())
                .orElseThrow(() -> ResourceNotFountException.of("Destination account", request.toAccountId()));

        if (from.getUser().getId()!=userId) {
            throw new IllegalArgumentException("Source account does not belong to you");
        }

        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient balance in '%s'. Available: ₹%s, Requested: ₹%s"
                            .formatted(from.getName(), from.getBalance(), request.amount())
            );
        }

        from.debit(request.amount());
        to.credit(request.amount());
        accountRepository.save(from);
        accountRepository.save(to);

        auditLogService.log("TRANSFER", "Account", from.getId(), userId, Map.of(
                "fromAccountId", from.getId(),
                "fromAccountName", from.getName(),
                "toAccountId", to.getId(),
                "toAccountName", to.getName(),
                "amount", request.amount(),
                "note", request.note() != null ? request.note() : ""
        ));

        log.info("Transfer of ₹{} from account {} to {} completed",
                request.amount(), from.getId(), to.getId());
    }

    // --- Internal helper ---

    private Account getOwnedAccount(Long userId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> ResourceNotFountException.of("Account", accountId));
        if (account.getUser().getId()!=userId) {
            throw new IllegalArgumentException("Account does not belong to you");
        }
        if (!account.isActive()) {
            throw new ResourceNotFountException("Account not found with id: " + accountId);
        }
        return account;
    }
}
