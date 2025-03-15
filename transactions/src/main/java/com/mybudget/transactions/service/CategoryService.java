package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.enums.TransactionType;

public interface CategoryService {
    void createUserCategory(String keycloakSub, String categoryName, TransactionType transactionType);
}
