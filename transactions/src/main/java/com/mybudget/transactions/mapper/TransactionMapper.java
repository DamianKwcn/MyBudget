package com.mybudget.transactions.mapper;

import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Transaction;

public class TransactionMapper {
    public static Transaction mapToTransaction(TransactionDto transactionDto, Transaction transaction){
        transaction.setAmount(transactionDto.getAmount());
        transaction.setBalanceAfter(transactionDto.getBalanceAfter());
        transaction.setTransactionType(transactionDto.getTransactionType());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setCreatedAt(transactionDto.getCreatedAt());
        return transaction;
    }

    public static TransactionDto mapToTransactionDto(Transaction transaction, TransactionDto transactionDto) {
        transactionDto.setAmount(transaction.getAmount());
        transactionDto.setBalanceAfter(transaction.getBalanceAfter());
        transactionDto.setTransactionType(transaction.getTransactionType());
        transactionDto.setDescription(transaction.getDescription());
        transactionDto.setCreatedAt(transaction.getCreatedAt());
        transactionDto.setCategoryName(transactionDto.getCategoryName());
        return transactionDto;
    }
}
