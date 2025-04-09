package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceAlreadyExistsException;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;

    @Override
    public void createUserCategory(String keycloakSub, String categoryName, TransactionType transactionType) {
        logger.info("Creating category for sub: {}, category: {}", keycloakSub, categoryName);

        Optional<Category> existing = categoryRepository.findByCategoryName(categoryName);
        if (existing.isPresent()) {
            logger.warn("Category with name {} already exists! Skipping creation.", categoryName);
            throw new ResourceAlreadyExistsException("Category", "categoryName", categoryName);
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setTransactionType(transactionType);
        category.setKeycloakSub(keycloakSub);
        category.setBuiltIn(false);
        categoryRepository.save(category);

        logger.info("Successfully created category for sub: {}, category: {}", keycloakSub, categoryName);
    }

    @Override
    public List<Category> getCategoriesByType(String keycloakSub, TransactionType transactionType) {
        logger.info("Searching expense type categories for sub: {}", keycloakSub);
        if (transactionType != TransactionType.EXPENSE && transactionType != TransactionType.INCOME) {
            throw new ResourceNotFoundException(keycloakSub, transactionType.toString(), "");
        }
        return categoryRepository.findByKeycloakSubAndTransactionType(keycloakSub, transactionType);
    }
}
