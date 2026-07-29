package com.krypto.financeadvisor.controller;

import com.krypto.financeadvisor.dto.request.CreateCategoryRequest;
import com.krypto.financeadvisor.dto.response.ApiResponse;
import com.krypto.financeadvisor.dto.response.CategoryResponse;
import com.krypto.financeadvisor.service.CategoryService;
import com.krypto.financeadvisor.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                categoryService.getAllCategories(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created",
                        categoryService.createCategory(userId, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok("Category updated",
                categoryService.updateCategory(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }
}
