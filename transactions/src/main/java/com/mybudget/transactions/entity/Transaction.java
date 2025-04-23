package com.mybudget.transactions.entity;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor @EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_sub", nullable = false)
    private String keycloakSub;

    @Column(name = "preferred_username")
    private String username;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = true)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false,
            insertable = false, updatable = false)
    private TransactionType transactionType;

    @PrePersist @PreUpdate
    private void syncType() {
        this.transactionType = category.getTransactionType();
    }

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

}