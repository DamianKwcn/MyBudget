package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.enums.TransactionType;

public interface CategoryCommandService {
    void createUserCategory(String categoryName, TransactionType transactionType);
    void deleteCategory(Long categoryId);
    void deleteAllByUsernameAfterDeletingAccount(String username);
    void seedDefaultCategoriesForUser(String keycloakSub, String username);
}

