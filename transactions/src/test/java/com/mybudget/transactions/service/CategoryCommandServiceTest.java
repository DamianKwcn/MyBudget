package com.mybudget.transactions.service;

import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceAlreadyExistsException;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.service.implementation.CategoryCommandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryCommandServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserProvider currentUser;

    @InjectMocks
    private CategoryCommandServiceImpl service;

    private final String sub = "sub";
    private final String username = "alice";

    @BeforeEach
    void setUp() {
        when(currentUser.getKeycloakSub()).thenReturn(sub);
        when(currentUser.getUsername()).thenReturn(username);
    }

    @Test
    void shouldCreateUserCategoryWhenNotExists() {
        // GIVEN
        when(categoryRepository.existsByKeycloakSubAndCategoryNameIgnoreCase(sub, "TestCat"))
                .thenReturn(false);

        // WHEN
        service.createUserCategory("TestCat", TransactionType.EXPENSE);

        // THEN
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        Category saved = captor.getValue();
        assertEquals("TestCat", saved.getCategoryName());
        assertEquals(sub, saved.getKeycloakSub());
        assertEquals(username, saved.getUsername());
        assertEquals(TransactionType.EXPENSE, saved.getTransactionType());
    }

    @Test
    void shouldThrowWhenCreatingExistingCategory() {
        // GIVEN
        when(categoryRepository.existsByKeycloakSubAndCategoryNameIgnoreCase(sub, "Dup"))
                .thenReturn(true);

        // WHEN / THEN
        assertThrows(ResourceAlreadyExistsException.class, () ->
                service.createUserCategory("Dup", TransactionType.INCOME)
        );
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCategoryWhenExists() {
        // GIVEN
        Category cat = new Category();
        cat.setId(7L);
        when(categoryRepository.findByIdAndKeycloakSub(7L, sub))
                .thenReturn(Optional.of(cat));

        // WHEN
        service.deleteCategory(7L);

        // THEN
        verify(categoryRepository).delete(cat);
    }

    @Test
    void shouldThrowWhenDeletingMissingCategory() {
        // GIVEN
        when(categoryRepository.findByIdAndKeycloakSub(8L, sub))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () ->
                service.deleteCategory(8L)
        );
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteAllByUsernameAfterDeletingAccount() {
        // WHEN
        service.deleteAllByUsernameAfterDeletingAccount("bob");

        // THEN
        verify(categoryRepository).deleteAllByUsername("bob");
    }

    @Test
    void shouldSeedDefaultCategoriesForUser() {
        // GIVEN
        when(categoryRepository.existsByKeycloakSubAndCategoryNameIgnoreCase(sub, "Salary"))
                .thenReturn(true);
        for (String name : List.of("Gift", "Groceries", "Transport", "Entertainment")) {
            when(categoryRepository.existsByKeycloakSubAndCategoryNameIgnoreCase(sub, name))
                    .thenReturn(false);
        }

        // WHEN
        service.seedDefaultCategoriesForUser(sub, username);

        // THEN
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(4)).save(captor.capture());
        List<String> savedNames = captor.getAllValues().stream()
                .map(Category::getCategoryName)
                .toList();
        assertTrue(savedNames.containsAll(List.of("Gift", "Groceries", "Transport", "Entertainment")));
    }
}
