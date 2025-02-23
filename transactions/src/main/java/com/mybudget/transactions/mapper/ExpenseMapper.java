package com.mybudget.transactions.mapper;

import com.mybudget.transactions.dto.ExpenseDto;
import com.mybudget.transactions.entity.Transaction;

public class ExpenseMapper {
    public static ExpenseDto mapToExpenseDto(Transaction transaction, ExpenseDto expenseDto) {
        expenseDto.setAmount(transaction.getAmount());
        expenseDto.setBalanceAfter(transaction.getBalanceAfter());
        expenseDto.setTransactionType(transaction.getTransactionType());
        expenseDto.setExpenseCategory(transaction.getExpenseCategory());
        expenseDto.setDescription(transaction.getDescription());
        expenseDto.setCreatedAt(transaction.getCreatedAt());
        return expenseDto;
    }

    public static Transaction mapToTransaction(ExpenseDto expenseDto, Transaction transaction) {
        transaction.setAmount(expenseDto.getAmount());
        transaction.setBalanceAfter(expenseDto.getBalanceAfter());
        transaction.setTransactionType(expenseDto.getTransactionType());
        transaction.setExpenseCategory(expenseDto.getExpenseCategory());
        transaction.setDescription(expenseDto.getDescription());
        transaction.setCreatedAt(expenseDto.getCreatedAt());
        return transaction;
    }
}
