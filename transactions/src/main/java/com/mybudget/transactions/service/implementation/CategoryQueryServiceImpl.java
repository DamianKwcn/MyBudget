package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryServiceImpl implements CategoryQueryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryQueryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUser;

    @Override
    public List<Category> findCategoriesByType(TransactionType transactionType) {
        String keycloakSub = currentUser.getKeycloakSub();
        logger.info("Searching categories for sub={} type={}", keycloakSub, transactionType);
        if (transactionType != TransactionType.EXPENSE && transactionType != TransactionType.INCOME) {
            throw new ResourceNotFoundException(keycloakSub, transactionType.toString(), "");
        }
        return categoryRepository.findAllByKeycloakSubAndTransactionType(keycloakSub, transactionType);
    }

    @Override
    public List<Category> findUserCategories() {
        String keycloakSub = currentUser.getKeycloakSub();
        logger.info("Loading all categories for sub={}", keycloakSub);
        return categoryRepository.findAllByKeycloakSub(currentUser.getKeycloakSub());
    }

    @Override
    public Category findUserCategory(Long categoryId) {
        String keycloakSub = currentUser.getKeycloakSub();
        logger.info("Retrieving category id={} for sub={}", categoryId, keycloakSub);
        return categoryRepository
                .findByIdAndKeycloakSub(categoryId, keycloakSub)
                .orElseThrow(() ->
                        new ResourceNotFoundException(keycloakSub, "categoryId", categoryId.toString()));
    }
}
