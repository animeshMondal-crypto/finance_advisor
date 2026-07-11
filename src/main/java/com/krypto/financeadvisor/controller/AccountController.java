// controller/AccountController.java
package com.krypto.financeadvisor.controller;

import com.krypto.financeadvisor.dto.request.CreateAccountRequest;
import com.krypto.financeadvisor.dto.request.TransferRequest;
import com.krypto.financeadvisor.dto.response.AccountResponse;
import com.krypto.financeadvisor.dto.response.ApiResponse;
import com.krypto.financeadvisor.util.SecurityUtil;
import com.krypto.financeadvisor.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created", accountService.createAccount(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(accountService.getUserAccounts(userId)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> transfer(
            @Valid @RequestBody TransferRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        accountService.transfer(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Transfer successful", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccount(userId, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long id,
            @RequestParam String name) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok("Account updated",
                accountService.updateAccount(userId, id, name)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        accountService.deleteAccount(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Account deleted", null));
    }

}