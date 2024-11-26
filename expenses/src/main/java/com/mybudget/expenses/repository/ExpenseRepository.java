package com.mybudget.expenses.repository;

import com.mybudget.expenses.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    Optional<Expense> findByMobileNumber(String mobileNumber);
    Optional<Expense> findByExpenseId(Long expenseId);
    Optional<Expense> findByMobileNumberAndTitle(String mobileNumber, String title);
}
