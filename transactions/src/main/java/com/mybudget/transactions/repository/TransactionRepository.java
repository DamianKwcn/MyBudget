package com.mybudget.transactions.repository;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByKeycloakSubAndCategoryId(String keycloakSub, Long categoryId);
    List<Transaction> findByKeycloakSubAndTransactionType(String keycloakSub, TransactionType transactionType);
    List<Transaction> findAllByKeycloakSub(String keyCloakSub);
    Optional<Transaction> findByKeycloakSubAndId(String keycloakSub, Long id);
    Optional<Transaction> findFirstByKeycloakSubAndStatusOrderByIdAsc(String keycloakSub, TransactionStatus status);
}
