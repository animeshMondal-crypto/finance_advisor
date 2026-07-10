package com.krypto.financeadvisor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String icon;

    @Column(length = 7)
    private String colorHex;

    // system = true  → default app categories (Food, Transport…), user_id is null
    // system = false → user-created custom categories, user_id is set
    @Column(nullable = false)
    @Builder.Default
    private boolean system = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")   // nullable for system categories
    private User user;
}
