package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.entity.feign.BalanceUpdateRequest;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.client.AccountsFeignClient;
import com.mybudget.transactions.service.implementation.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountsFeignClient accountsFeignClient;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final String keycloakSub = "user-123";

    @Test
    void shouldFindTransactionById() {
        // GIVEN
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setId(1L);
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.of(transaction));

        // WHEN
        Optional<Transaction> foundTransaction = transactionService.findTransaction(keycloakSub, 1L);

        // THEN
        assertTrue(foundTransaction.isPresent());
        assertEquals(transaction, foundTransaction.get());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // GIVEN
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> transactionService.findTransaction(keycloakSub, 1L));
    }

    @Test
    void shouldCreateExpenseTransaction() {
        // GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);

        BigDecimal amount = BigDecimal.valueOf(100);
        Category expenseCategory = new Category();
        expenseCategory.setId(3L);
        expenseCategory.setCategoryName("Car");
        expenseCategory.setTransactionType(TransactionType.EXPENSE);
        when(categoryRepository.findById(eq(3L))).thenReturn(Optional.of(expenseCategory));
        when(accountsFeignClient.getUserBalance(anyString(), eq(keycloakSub)))
                .thenReturn(BigDecimal.valueOf(1000));

        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.EXPENSE);
        transaction.setCategory(expenseCategory);
        transaction.setDescription("Home expense");
        transaction.setBalanceAfter(BigDecimal.valueOf(900));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // WHEN
        Transaction createdTransaction = transactionService.createTransaction(keycloakSub, amount, 3L, "Home expense");

        // THEN
        assertNotNull(createdTransaction);
        assertEquals(amount, createdTransaction.getAmount());
        assertEquals(TransactionType.EXPENSE, createdTransaction.getTransactionType());
        assertEquals("Car", createdTransaction.getCategory().getCategoryName());
        assertEquals(BigDecimal.valueOf(900), createdTransaction.getBalanceAfter());
        verify(accountsFeignClient).updateBalance(eq("Bearer mocked-token"), any(BalanceUpdateRequest.class));
    }

    @Test
    void shouldCreateIncomeTransaction() {
        // GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);

        BigDecimal amount = BigDecimal.valueOf(200);
        Category incomeCategory = new Category();
        incomeCategory.setId(1L);
        incomeCategory.setCategoryName("Salary");
        incomeCategory.setTransactionType(TransactionType.INCOME);
        when(categoryRepository.findById(eq(1L))).thenReturn(Optional.of(incomeCategory));
        when(accountsFeignClient.getUserBalance(anyString(), eq(keycloakSub)))
                .thenReturn(BigDecimal.valueOf(1000));

        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.INCOME);
        transaction.setCategory(incomeCategory);
        transaction.setDescription("Salary");
        transaction.setBalanceAfter(BigDecimal.valueOf(1200));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // WHEN
        Transaction createdTransaction = transactionService.createTransaction(keycloakSub, amount, 1L, "Salary");

        // THEN
        assertNotNull(createdTransaction);
        assertEquals(amount, createdTransaction.getAmount());
        assertEquals(TransactionType.INCOME, createdTransaction.getTransactionType());
        assertEquals("Salary", createdTransaction.getCategory().getCategoryName());
        assertEquals(BigDecimal.valueOf(1200), createdTransaction.getBalanceAfter());
        verify(accountsFeignClient).updateBalance(eq("Bearer mocked-token"), any(BalanceUpdateRequest.class));
    }

    @Test
    void shouldFindTransactionsByType() {
        // GIVEN
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setTransactionType(TransactionType.INCOME);
        when(transactionRepository.findByKeycloakSubAndTransactionType(keycloakSub, TransactionType.INCOME))
                .thenReturn(List.of(transaction));

        // WHEN
        List<Transaction> transactions = transactionService.findByTransactionType(keycloakSub, TransactionType.INCOME);

        // THEN
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldFindAllTransactionsByUser() {
        // GIVEN
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        when(transactionRepository.findTransactionsByKeycloakSub(keycloakSub))
                .thenReturn(List.of(transaction));

        // WHEN
        List<Transaction> transactions = transactionService.findTransactions(keycloakSub);

        // THEN
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldDeleteTransaction() {
        // GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(150));
        transaction.setTransactionType(TransactionType.EXPENSE);
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.of(transaction));

        // WHEN
        boolean deleted = transactionService.deleteTransaction(keycloakSub, 1L);

        // THEN
        assertTrue(deleted, "Should return true after deletion.");
        verify(transactionRepository).findTransactionByKeycloakSubAndId(keycloakSub, 1L);
        verify(accountsFeignClient).updateBalanceAfterDelete(eq("Bearer mocked-token"),
                argThat(req ->
                        req.getKeycloakSub().equals(keycloakSub)
                                && req.getAmount().equals(BigDecimal.valueOf(150))
                                && req.getTransactionType().equals(TransactionType.EXPENSE)
                ));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTransaction() {
        // GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(keycloakSub, 1L));
        verify(transactionRepository, never()).delete(any(Transaction.class));
        verify(accountsFeignClient, never()).updateBalanceAfterDelete(anyString(), any(BalanceUpdateRequest.class));
    }
}
