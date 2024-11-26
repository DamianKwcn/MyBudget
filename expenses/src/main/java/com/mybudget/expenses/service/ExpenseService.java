package com.mybudget.expenses.service;

public interface ExpenseService {
    void createExpense(String mobileNumber);
    boolean deleteExpense(Long expenseId);
}
