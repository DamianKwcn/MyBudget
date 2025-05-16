package com.mybudget.transactions.service;

import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.implementation.TransactionQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TransactionQueryServiceImpl queryService;

    private final String sub = "testSub";
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setKeycloakSub(sub);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setTransactionType(TransactionType.INCOME);

        transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setKeycloakSub(sub);
        transaction2.setAmount(BigDecimal.valueOf(20));
        transaction2.setTransactionType(TransactionType.EXPENSE);
    }

    @Test
    void shouldFindTransactionByKeycloakSubAndId() {
        // GIVEN
        when(currentUserProvider.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findByKeycloakSubAndId(sub, 1L))
                .thenReturn(Optional.of(transaction1));

        // WHEN
        Transaction found = queryService.findByKeycloakSubAndId(1L);

        // THEN
        assertNotNull(found);
        assertEquals(1L, found.getId());
        verify(transactionRepository).findByKeycloakSubAndId(sub, 1L);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // GIVEN
        when(currentUserProvider.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findByKeycloakSubAndId(sub, 99L))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                queryService.findByKeycloakSubAndId(99L)
        );
        verify(transactionRepository).findByKeycloakSubAndId(sub, 99L);
    }

    @Test
    void shouldFindTransactionsByCategory() {
        // GIVEN
        when(currentUserProvider.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findAllByKeycloakSubAndCategoryId(sub, 5L))
                .thenReturn(List.of(transaction1, transaction2));

        // WHEN
        List<Transaction> list = queryService.findByCategory(5L);

        // THEN
        assertEquals(2, list.size());
        verify(transactionRepository).findAllByKeycloakSubAndCategoryId(sub, 5L);
    }

    @Test
    void shouldFindTransactionsByTransactionType() {
        // GIVEN
        when(currentUserProvider.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findByKeycloakSubAndTransactionType(sub, TransactionType.INCOME))
                .thenReturn(List.of(transaction1));

        // WHEN
        List<Transaction> list = queryService.findByTransactionType(TransactionType.INCOME);

        // THEN
        assertEquals(1, list.size());
        verify(transactionRepository).findByKeycloakSubAndTransactionType(sub, TransactionType.INCOME);
    }

    @Test
    void shouldFindAllTransactions() {
        // GIVEN
        when(currentUserProvider.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findAllByKeycloakSub(sub))
                .thenReturn(List.of(transaction1, transaction2));

        // WHEN
        List<Transaction> list = queryService.findTransactions();

        // THEN
        assertEquals(2, list.size());
        verify(transactionRepository).findAllByKeycloakSub(sub);
    }
}
