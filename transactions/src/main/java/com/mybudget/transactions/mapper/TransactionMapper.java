package com.mybudget.transactions.mapper;

import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Transaction;

public class TransactionMapper {
    public static Transaction mapToTransaction(TransactionDto transactionDto, Transaction transaction){
        transaction.setAmount(transactionDto.getAmount());
        transaction.setBalanceAfter(transactionDto.getBalanceAfter());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setCreatedAt(transactionDto.getCreatedAt());
        return transaction;
    }

    public static TransactionDto mapToTransactionDto(Transaction tx, TransactionDto dto) {
        dto.setAmount(tx.getAmount());
        dto.setBalanceAfter(tx.getBalanceAfter());
        dto.setTransactionType(tx.getCategory().getTransactionType());
        dto.setDescription(tx.getDescription());
        dto.setCreatedAt(tx.getCreatedAt());
        dto.setCategoryName(tx.getCategory().getCategoryName());
        return dto;
    }
}
