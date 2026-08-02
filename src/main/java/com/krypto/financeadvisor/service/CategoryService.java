package com.krypto.financeadvisor.service;

import com.krypto.financeadvisor.dto.request.CreateCategoryRequest;
import com.krypto.financeadvisor.dto.response.CategoryResponse;
import com.krypto.financeadvisor.entity.Category;
import com.krypto.financeadvisor.entity.User;
import com.krypto.financeadvisor.exception.ResourceNotFountException;
import com.krypto.financeadvisor.repository.CategoryRepository;
import com.krypto.financeadvisor.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

//    private final EntityManager entityManager;

    // Returns system categories + user's own custom categories
    @Cacheable(
            value = "categories",
            key = "#userId"
    )
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Long userId) {
        return categoryRepository.findAllVisibleToUser(userId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @CacheEvict(
            value = "categories",
            key = "#userId"
    )
    @Transactional
    public CategoryResponse createCategory(Long userId, CreateCategoryRequest request) {
        if (categoryRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new IllegalArgumentException(
                    "You already have a category named: " + request.name());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFountException.of("User", userId));

        Category category = Category.builder()
                .name(request.name())
                .icon(request.icon())
                .colorHex(request.colorHex())
                .system(false)       // user-created categories are never system
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);

        auditLogService.log("CREATE_CATEGORY", "Category", saved.getId(), userId,
                Map.of("name", saved.getName()));

        return CategoryResponse.from(saved);
    }

    @CacheEvict(value = "categories", key = "#userId")
    @Transactional
    public CategoryResponse updateCategory(Long userId, Long categoryId,
                                           CreateCategoryRequest request) {
        Category category = getOwnedCategory(userId, categoryId);

        category.setName(request.name());

        if (request.icon() != null) {
            category.setIcon(request.icon());
        }
        if (request.colorHex() != null) {
            category.setColorHex(request.colorHex());
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    // -------------------------------------------------------
    // DELETE — demonstrates two transaction concepts together:
    //
    // 1. ATOMICITY (@Transactional)
    //    Reassigning orphaned transactions AND deleting the
    //    category happen in one transaction. If either step
    //    fails, neither change is committed.
    //
    // 2. PROPAGATION (AuditLogService.REQUIRES_NEW)
    //    Audit log commits independently. Even if the delete
    //    rolls back, the attempt is recorded.
    //
    // Why reassign instead of cascade delete?
    //    Deleting a category should NOT delete the user's
    //    transaction history — that would be a data loss bug.
    //    Instead we set category to null on all transactions
    //    that used this category, preserving the records.
    // -------------------------------------------------------

    @Caching(evict = {
            @CacheEvict(value = "categories",   key = "#userId"),
            @CacheEvict(value = "transactions", key = "#userId")
    })
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        log.debug("Cache EVICT — categories + transactions for user {} (category deleted)", userId);

        Category category = getOwnedCategory(userId, categoryId);

        // Reassign all transactions using this category to uncategorized (null)
        // ON DELETE SET NULL is already in the DB schema, but we also
        // handle it explicitly here for clarity and auditability

//        entityManager.createQuery("UPDATE Transaction t SET t.category = null WHERE t.category.id = :categoryId")
//                .setParameter("categoryId", categoryId)
//                .executeUpdate();

        categoryRepository.delete(category);

        auditLogService.log("DELETE_CATEGORY", "Category", categoryId, userId,
                Map.of("name", category.getName()));

        log.info("Category {} deleted, transactions reassigned to uncategorized", categoryId);
    }

    // --- Internal helper ---

    private Category getOwnedCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFountException.of("Category", categoryId));

        // Prevent modifying or deleting system categories
        if (category.isSystem()) {
            throw new IllegalArgumentException("System categories cannot be modified or deleted");
        }

        // Prevent modifying another user's custom category
        if (category.getUser() == null || category.getUser().getId()!=userId) {
            throw new IllegalArgumentException("Category does not belong to you");
        }

        return category;
    }
}
