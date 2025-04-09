package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;

import java.util.List;

public interface CategoryService {
    void createUserCategory(String keycloakSub, String categoryName, TransactionType transactionType);

    List<Category> getCategoriesByType(String keycloakSub, TransactionType transactionType);
}
