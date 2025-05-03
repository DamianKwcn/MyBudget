package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction findByKeycloakSubAndId(String keycloakSub, Long id);

    List<Transaction> findByCategory(String keycloakSub, Long categoryId);

    Transaction createTransaction(String keycloakSub, BigDecimal amount, Long categoryId, String description);

    List<Transaction> findByTransactionType(String keycloakSub, TransactionType transactionType);

    List<Transaction> findTransactions(String keycloakSub);

    void deleteTransaction(String keycloakSub, Long id);
}