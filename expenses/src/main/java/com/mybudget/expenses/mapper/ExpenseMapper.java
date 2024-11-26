package com.mybudget.expenses.mapper;

import com.mybudget.expenses.dto.ExpenseDto;
import com.mybudget.expenses.entity.Expense;

public class ExpenseMapper {
    public static Expense mapToExpense(ExpenseDto expenseDto, Expense expense) {
        expense.setMobileNumber(expenseDto.getMobileNumber());
        expense.setTitle(expenseDto.getTitle());
        expense.setDescription(expenseDto.getDescription());
        expense.setAmountUsed(expenseDto.getAmountUsed());
        return expense;
    }

    public static ExpenseDto mapToExpenseDto(Expense expense, ExpenseDto expenseDto) {
        expenseDto.setMobileNumber(expense.getMobileNumber());
        expenseDto.setTitle(expense.getTitle());
        expenseDto.setDescription(expense.getDescription());
        expenseDto.setAmountUsed(expense.getAmountUsed());
        return expenseDto;
    }
}
