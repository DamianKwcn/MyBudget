package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;

import java.util.List;

public interface TransactionQueryService {
    Transaction findByKeycloakSubAndId(Long id);
    List<Transaction> findByCategory(Long categoryId);
    List<Transaction> findByTransactionType(TransactionType transactionType);
    List<Transaction> findTransactions();
}
