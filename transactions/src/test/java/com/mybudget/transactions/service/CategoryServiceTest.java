package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceAlreadyExistsException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.implementation.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private final String keycloakSub = "user-123";
    private final String categoryName = "Test";
    private final TransactionType transactionType = TransactionType.EXPENSE;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(10L);
        category.setCategoryName(categoryName);
        category.setTransactionType(transactionType);
        category.setKeycloakSub(keycloakSub);
        category.setBuiltIn(false);
    }

    @Nested
    @DisplayName("createUserCategory()")
    class CreateUserCategoryTests {

        @Test
        @DisplayName("Should create new category when it does not exist")
        void shouldCreateNewCategory() {
            // GIVEN
            when(categoryRepository.findByCategoryName(eq(categoryName))).thenReturn(Optional.empty());

            // WHEN
            categoryService.createUserCategory(keycloakSub, categoryName, transactionType);

            // THEN
            verify(categoryRepository, times(1)).findByCategoryName(eq(categoryName));
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when category already exists")
        void shouldThrowExceptionWhenCategoryExists() {
            // GIVEN
            when(categoryRepository.findByCategoryName(eq(categoryName))).thenReturn(Optional.of(category));

            // WHEN & THEN
            assertThrows(ResourceAlreadyExistsException.class,
                    () -> categoryService.createUserCategory(keycloakSub, categoryName, transactionType));

            verify(categoryRepository, times(1)).findByCategoryName(eq(categoryName));
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }
}
