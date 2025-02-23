package com.mybudget.transactions.mapper;

import com.mybudget.transactions.dto.IncomeDto;
import com.mybudget.transactions.entity.Transaction;

public class IncomeMapper {
    public static Transaction mapToTransaction(IncomeDto incomeDto, Transaction transaction){
        transaction.setAmount(incomeDto.getAmount());
        transaction.setBalanceAfter(incomeDto.getBalanceAfter());
        transaction.setTransactionType(incomeDto.getTransactionType());
        transaction.setIncomeCategory(incomeDto.getIncomeCategory());
        transaction.setDescription(incomeDto.getDescription());
        transaction.setCreatedAt(incomeDto.getCreatedAt());
        return transaction;
    }

    public static IncomeDto mapToIncomeDto(Transaction transaction, IncomeDto incomeDto) {
        incomeDto.setAmount(transaction.getAmount());
        incomeDto.setBalanceAfter(transaction.getBalanceAfter());
        incomeDto.setTransactionType(transaction.getTransactionType());
        incomeDto.setIncomeCategory(transaction.getIncomeCategory());
        incomeDto.setDescription(transaction.getDescription());
        incomeDto.setCreatedAt(transaction.getCreatedAt());
        return incomeDto;
    }
}
