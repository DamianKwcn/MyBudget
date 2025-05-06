package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;

import java.util.List;

public interface CategoryQueryService {
    List<Category> findCategoriesByType(TransactionType transactionType);
    List<Category> findUserCategories();
    Category findUserCategory(Long categoryId);
}
