package com.mybudget.transactions.entity;

import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"keycloak_sub", "category_name"})
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_sub")
    private String keycloakSub;

    @Column(nullable = false, name = "category_name")
    private String categoryName;

    @Column(name = "preferred_username")
    private String username;

    @Column(nullable = false,
            columnDefinition = "NUMERIC(19,2) DEFAULT 0")
    @org.hibernate.annotations.ColumnDefault("0")
    private BigDecimal categoryBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setCategory(this);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setCategory(null);
    }
}
