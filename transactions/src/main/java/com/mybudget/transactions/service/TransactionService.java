package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Optional<Transaction> findTransaction(String keycloakSub, Long id);

    void deleteAllByUsername(String username);

    Transaction createTransaction(String keycloakSub, String username, BigDecimal amount, Long categoryId, String description);

    List<Transaction> findByTransactionType(String keycloakSub, TransactionType transactionType);

    List<Transaction> findTransactions(String keycloakSub);

    boolean deleteTransaction(String keycloakSub, Long id);

    void deleteAllByKeycloakSub(String keycloakSub);
}