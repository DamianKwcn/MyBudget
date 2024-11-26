package com.mybudget.expenses.service.impl;

import com.mybudget.expenses.repository.ExpenseRepository;
import com.mybudget.expenses.service.ExpenseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private ExpenseRepository expenseRepository;

    @Override
    public void createExpense(String mobileNumber) {
    }

    @Override
    public boolean deleteExpense(Long expenseId) {
        return false;
    }
}
