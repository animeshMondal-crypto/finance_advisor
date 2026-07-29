package com.krypto.financeadvisor.controller;

import com.krypto.financeadvisor.dto.request.CreateBudgetRequest;
import com.krypto.financeadvisor.dto.request.UpdateBudgetRequest;
import com.krypto.financeadvisor.dto.response.ApiResponse;
import com.krypto.financeadvisor.dto.response.BudgetResponse;
import com.krypto.financeadvisor.service.BudgetService;
import com.krypto.financeadvisor.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody CreateBudgetRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Budget created",
                        budgetService.createBudget(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getAllBudgets(userId)));
    }

    @PostMapping("/rollover")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> rollover() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok("Budgets rolled over",
                budgetService.rolloverBudgets(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudget(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getBudget(userId, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok("Budget updated",
                budgetService.updateBudget(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Budget deleted", null));
    }
}
