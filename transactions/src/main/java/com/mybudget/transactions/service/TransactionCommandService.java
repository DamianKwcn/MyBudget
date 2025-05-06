package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Transaction;

import java.math.BigDecimal;

public interface TransactionCommandService {
    Transaction createTransaction(BigDecimal amount, Long categoryId, String description);
    void deleteTransaction(Long id);
}