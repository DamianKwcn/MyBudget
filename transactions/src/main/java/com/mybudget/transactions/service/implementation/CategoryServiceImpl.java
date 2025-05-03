package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceAlreadyExistsException;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createUserCategory(String categoryName, TransactionType transactionType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No JWT provided");
        }
        String keycloakSub = jwtAuth.getToken().getSubject();
        String username = jwtAuth.getToken().getClaimAsString("preferred_username");

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
    @Transactional(readOnly = true)
    public List<Category> findCategoriesByType(String keycloakSub, TransactionType transactionType) {
        logger.info("Searching categories for sub={} type={}", keycloakSub, transactionType);
        if (transactionType != TransactionType.EXPENSE && transactionType != TransactionType.INCOME) {
            throw new ResourceNotFoundException(keycloakSub, transactionType.toString(), "");
        }
        return categoryRepository.findByKeycloakSubAndTransactionType(keycloakSub, transactionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findUserCategories(String keycloakSub) {
        logger.info("Loading all categories for sub={}", keycloakSub);
        return categoryRepository.findByKeycloakSub(keycloakSub);
    }

    @Override
    @Transactional
    public void deleteCategory(String keycloakSub, Long categoryId) {
        Category category = categoryRepository
                .findByIdAndKeycloakSub(categoryId, keycloakSub)
                .orElseThrow(() ->
                        new ResourceNotFoundException(keycloakSub, "categoryId", categoryId.toString()));

        categoryRepository.delete(category);
        logger.info("Deleted category id={} for sub={}", categoryId, keycloakSub);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findUserCategory(String keycloakSub, Long categoryId) {
        logger.info("Retrieving category id={} for sub={}", categoryId, keycloakSub);
        return categoryRepository.findByIdAndKeycloakSub(categoryId, keycloakSub)
                .orElseThrow(() ->
                        new ResourceNotFoundException(keycloakSub, "categoryId", categoryId.toString())
                );
    }

    @Override
    @Transactional
    public void deleteAllByUsernameAfterDeletingAccount(String username) {
        categoryRepository.deleteAllByUsername(username);
        logger.info("Deleted all categories for username={}", username);
    }

    @Override
    @Transactional
    public void seedDefaultCategoriesForUser(String keycloakSub, String username) {
        for (CategoryTemplate tpl : DEFAULTS) {
            if (categoryRepository.existsByKeycloakSubAndCategoryNameIgnoreCase(keycloakSub, tpl.name())) {
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
            new CategoryTemplate("Gift", TransactionType.INCOME),
            new CategoryTemplate("Groceries", TransactionType.EXPENSE),
            new CategoryTemplate("Transport", TransactionType.EXPENSE),
            new CategoryTemplate("Entertainment", TransactionType.EXPENSE)
    );

    private record CategoryTemplate(String name, TransactionType type) {}
}
