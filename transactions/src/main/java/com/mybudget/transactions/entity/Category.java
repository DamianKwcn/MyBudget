package com.mybudget.transactions.entity;

import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "category_name")
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = true)
    private String keycloakSub;

    @Column(name = "built_in", nullable = false, updatable = false)
    private Boolean builtIn;
}
