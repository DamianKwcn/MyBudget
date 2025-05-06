package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceAlreadyExistsException;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.CategoryCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandServiceImpl implements CategoryCommandService {
    private final CategoryRepository categoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategoryCommandServiceImpl.class);
    private final CurrentUserProvider currentUser;

    @Override
    public void createUserCategory(String categoryName, TransactionType transactionType) {
        String keycloakSub = currentUser.getKeycloakSub();
        String username = currentUser.getUsername();

        logger.info("Creating category for sub={} category={}", keycloakSub, categoryName);

        if (categoryRepository.existsByKeycloakSubAndCategoryNameIgnoreCase(keycloakSub, categoryName)) {
            throw new ResourceAlreadyExistsException("Category", "categoryName", categoryName);
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setUsername(username);
        category.setKeycloakSub(keycloakSub);
        category.setTransactionType(transactionType);
        categoryRepository.save(category);

        logger.info("Successfully created category for sub={} category={}", keycloakSub, categoryName);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        String keycloakSub = currentUser.getKeycloakSub();
        Category category = categoryRepository
                .findByIdAndKeycloakSub(categoryId, keycloakSub)
                .orElseThrow(() ->
                        new ResourceNotFoundException(keycloakSub, "categoryId", categoryId.toString()));
        categoryRepository.delete(category);
        logger.info("Deleted category id={} for sub={}", categoryId, keycloakSub);
    }

    @Override
    public void deleteAllByUsernameAfterDeletingAccount(String username) {
        categoryRepository.deleteAllByUsername(username);
        logger.info("Deleted all categories for username={}", username);
    }

    @Override
    public void seedDefaultCategoriesForUser(String keycloakSub, String username) {
        for (CategoryTemplate tpl : DEFAULTS) {
            if (categoryRepository
                    .existsByKeycloakSubAndCategoryNameIgnoreCase(keycloakSub, tpl.name())) {
                continue;
            }
            Category c = new Category();
            c.setKeycloakSub(keycloakSub);
            c.setUsername(username);
            c.setCategoryName(tpl.name());
            c.setTransactionType(tpl.type());
            categoryRepository.save(c);
        }
        logger.info("Default categories seeded for sub={}", keycloakSub);
    }

    private static final List<CategoryTemplate> DEFAULTS = List.of(
            new CategoryTemplate("Salary", TransactionType.INCOME),
            new CategoryTemplate("Gift",   TransactionType.INCOME),
            new CategoryTemplate("Groceries",   TransactionType.EXPENSE),
            new CategoryTemplate("Transport",   TransactionType.EXPENSE),
            new CategoryTemplate("Entertainment", TransactionType.EXPENSE)
    );

    private record CategoryTemplate(String name, TransactionType type) {}
}
