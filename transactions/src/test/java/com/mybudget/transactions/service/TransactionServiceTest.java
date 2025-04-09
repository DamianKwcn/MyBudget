package com.mybudget.transactions.service;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.TransactionSagaStartEvent;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.implementation.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private String sub;

    @BeforeEach
    void setUp() {
        sub = "user123";
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        Jwt jwt = Jwt.withTokenValue("fake-token").header("alg", "none").claim("sub", "test-sub").build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        // given
        BigDecimal amount = BigDecimal.valueOf(100);
        Long categoryId = 1L;
        String description = "Test Transaction";
        String type = "income";
        Category category = new Category(categoryId, "Food", TransactionType.INCOME, sub, false);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // when
        Transaction result = transactionService.createTransaction(sub, amount, categoryId, description, type);

        // then
        assertEquals(sub, result.getKeycloakSub());
        assertEquals(TransactionType.INCOME, result.getTransactionType());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(kafkaTemplate).send(eq("orchestrator-commands"), any(TransactionSagaStartEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryBelongsToDifferentUser() {
        // given
        String otherSub = "otherUser";
        Long categoryId = 1L;
        Category category = new Category(categoryId, "Transport", TransactionType.EXPENSE, otherSub, false);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(sub, BigDecimal.TEN, categoryId, "desc", "EXPENSE")
        );

        verify(transactionRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void shouldFindTransactionByIdAndSub() {
        // given
        Long id = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setKeycloakSub(sub);

        when(transactionRepository.findTransactionByKeycloakSubAndId(sub, id)).thenReturn(Optional.of(transaction));

        // when
        Optional<Transaction> result = transactionService.findTransaction(sub, id);

        // then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldThrowResourceNotFoundWhenTransactionNotFound() {
        // given
        Long id = 999L;
        when(transactionRepository.findTransactionByKeycloakSubAndId(sub, id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> transactionService.findTransaction(sub, id));
    }

    @Test
    void shouldFindTransactionsByType() {
        // given
        TransactionType type = TransactionType.INCOME;
        List<Transaction> expected = List.of(new Transaction(), new Transaction());
        when(transactionRepository.findByKeycloakSubAndTransactionType(sub, type)).thenReturn(expected);

        // when
        List<Transaction> result = transactionService.findByTransactionType(sub, type);

        // then
        assertEquals(2, result.size());
    }

    @Test
    void shouldDeleteAllTransactionsBySub() {
        // when
        transactionService.deleteAllByKeycloakSub(sub);

        // then
        verify(transactionRepository).deleteByKeycloakSub(sub);
    }

    @Test
    void shouldFindAllTransactionsBySub() {
        // given
        List<Transaction> list = List.of(new Transaction(), new Transaction());
        when(transactionRepository.findTransactionsByKeycloakSub(sub)).thenReturn(list);

        // when
        List<Transaction> result = transactionService.findTransactions(sub);

        // then
        assertEquals(2, result.size());
    }

    @Test
    void shouldDeleteTransactionByIdAndSub() {
        // given
        Long id = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setKeycloakSub(sub);

        when(transactionRepository.findTransactionByKeycloakSubAndId(sub, id)).thenReturn(Optional.of(transaction));

        // when
        boolean deleted = transactionService.deleteTransaction(sub, id);

        // then
        assertTrue(deleted);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentTransaction() {
        // given
        Long id = 42L;
        when(transactionRepository.findTransactionByKeycloakSubAndId(sub, id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(sub, id));
    }
}
