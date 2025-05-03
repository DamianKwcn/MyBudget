package com.mybudget.transactions.repository;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCategoryIdAndKeycloakSub(Long categoryId, String keycloakSub);

    List<Transaction> findByKeycloakSubAndTransactionType(String keycloakSub, TransactionType transactionType);

    List<Transaction> findTransactionsByKeycloakSub(String keyCloakSub);

    Optional<Transaction> findTransactionByKeycloakSubAndId(String keycloakSub, Long id);

    void deleteByKeycloakSub(String keycloakSub);

    Optional<Transaction> findFirstByKeycloakSubAndStatusOrderByIdAsc(String keycloakSub, TransactionStatus status);

}
