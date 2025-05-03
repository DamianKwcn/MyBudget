package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    void createUserCategory(String categoryName, TransactionType transactionType);

    List<Category> findCategoriesByType(String keycloakSub, TransactionType transactionType);

    List<Category> findUserCategories(String keycloakSub);

    void deleteCategory(String keycloakSub, Long categoryId);

    Category findUserCategory(String keycloakSub, Long categoryId);

    void deleteAllByUsernameAfterDeletingAccount(String username);

    void seedDefaultCategoriesForUser(String keycloakSub, String username);

}
