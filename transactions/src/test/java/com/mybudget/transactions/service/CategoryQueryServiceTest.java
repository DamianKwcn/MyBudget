package com.mybudget.transactions.service;

import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.implementation.CategoryQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserProvider currentUser;

    @InjectMocks
    private CategoryQueryServiceImpl service;

    private final String sub = "sub";
    private Category catA;
    private Category catB;

    @BeforeEach
    void setUp() {
        when(currentUser.getKeycloakSub()).thenReturn(sub);

        catA = new Category();
        catA.setId(1L);
        catA.setKeycloakSub(sub);
        catA.setCategoryName("A");
        catA.setTransactionType(TransactionType.EXPENSE);

        catB = new Category();
        catB.setId(2L);
        catB.setKeycloakSub(sub);
        catB.setCategoryName("B");
        catB.setTransactionType(TransactionType.EXPENSE);
    }

    @Test
    void shouldReturnExpenseCategories_whenFindCategoriesByTypeCalled() {
        // GIVEN
        List<Category> expenses = List.of(catA, catB);
        when(categoryRepository.findAllByKeycloakSubAndTransactionType(sub, TransactionType.EXPENSE))
                .thenReturn(expenses);

        // WHEN
        List<Category> result = service.findCategoriesByType(TransactionType.EXPENSE);

        // THEN
        assertSame(expenses, result);
        verify(categoryRepository)
                .findAllByKeycloakSubAndTransactionType(sub, TransactionType.EXPENSE);
    }

    @Test
    void shouldReturnIncomeCategories_whenFindCategoriesByTypeCalledWithIncome() {
        // GIVEN
        Category inc1 = new Category();
        inc1.setId(3L);
        inc1.setKeycloakSub(sub);
        inc1.setCategoryName("Salary");
        inc1.setTransactionType(TransactionType.INCOME);

        Category inc2 = new Category();
        inc2.setId(4L);
        inc2.setKeycloakSub(sub);
        inc2.setCategoryName("Bonus");
        inc2.setTransactionType(TransactionType.INCOME);

        List<Category> incomes = List.of(inc1, inc2);
        when(categoryRepository.findAllByKeycloakSubAndTransactionType(sub, TransactionType.INCOME))
                .thenReturn(incomes);

        // WHEN
        List<Category> result = service.findCategoriesByType(TransactionType.INCOME);

        // THEN
        assertSame(incomes, result);
        verify(categoryRepository)
                .findAllByKeycloakSubAndTransactionType(sub, TransactionType.INCOME);
    }

    @Test
    void shouldReturnAllUserCategories_whenFindUserCategoriesCalled() {
        // GIVEN
        List<Category> all = List.of(catA);
        when(categoryRepository.findAllByKeycloakSub(sub))
                .thenReturn(all);

        // WHEN
        List<Category> result = service.findUserCategories();

        // THEN
        assertSame(all, result);
        verify(categoryRepository).findAllByKeycloakSub(sub);
    }

    @Test
    void shouldReturnCategory_whenFindUserCategoryExists() {
        // GIVEN
        when(categoryRepository.findByIdAndKeycloakSub(5L, sub))
                .thenReturn(Optional.of(catA));

        // WHEN
        Category result = service.findUserCategory(5L);

        // THEN
        assertSame(catA, result);
        verify(categoryRepository).findByIdAndKeycloakSub(5L, sub);
    }

    @Test
    void shouldThrowResourceNotFound_whenFindUserCategoryDoesNotExist() {
        // GIVEN
        when(categoryRepository.findByIdAndKeycloakSub(9L, sub))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () ->
                service.findUserCategory(9L)
        );
        verify(categoryRepository).findByIdAndKeycloakSub(9L, sub);
    }
}
