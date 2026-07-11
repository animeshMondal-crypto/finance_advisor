package com.krypto.financeadvisor.repository;

import com.krypto.financeadvisor.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Returns system categories (shared) + this user's custom categories
    // system categories have user_id = null so we check both conditions
    @Query("""
            SELECT c FROM Category c
            WHERE c.system = true
               OR c.user.id = :userId
            ORDER BY c.system DESC, c.name ASC
            """)
    List<Category> findAllVisibleToUser(@Param("userId") Long userId);

    boolean existsByNameAndUserId(String name, Long userId);
}
