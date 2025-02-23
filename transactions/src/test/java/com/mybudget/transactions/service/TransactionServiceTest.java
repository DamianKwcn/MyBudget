package com.mybudget.transactions.service;

import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.ExpenseCategory;
import com.mybudget.transactions.entity.enums.IncomeCategory;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.entity.feign.BalanceUpdateRequest;
import com.mybudget.transactions.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

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
        //GIVEN
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setId(1L);

        //WHEN
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.of(transaction));

        //THEN
        Optional<Transaction> foundTransaction = transactionService.findTransaction(keycloakSub, 1L);
        assertTrue(foundTransaction.isPresent());
        assertEquals(transaction, foundTransaction.get());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        //GIVEN && WHEN
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.empty());

        //THEN
        assertThrows(ResourceNotFoundException.class, () -> transactionService.findTransaction(keycloakSub, 1L));
    }

    @Test
    void shouldCreateExpenseTransaction() {
        //GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        BigDecimal amount = BigDecimal.valueOf(100);
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.EXPENSE);
        transaction.setExpenseCategory(ExpenseCategory.HOME);

        //WHEN
        when(accountsFeignClient.getUserBalance(anyString(), eq(keycloakSub)))
                .thenReturn(BigDecimal.valueOf(1000));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction createdTransaction = transactionService.createExpense(keycloakSub, amount,
                TransactionType.EXPENSE, ExpenseCategory.HOME, "Home expense");

        //THEN
        assertNotNull(createdTransaction);
        assertEquals(amount, createdTransaction.getAmount());
        verify(accountsFeignClient).updateBalance(eq("Bearer mocked-token"), any(BalanceUpdateRequest.class));
    }

    @Test
    void shouldCreateIncomeTransaction() {
        //GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);

        BigDecimal amount = BigDecimal.valueOf(200);
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.INCOME);
        transaction.setIncomeCategory(IncomeCategory.SALARY);

        //WHEN
        when(accountsFeignClient.getUserBalance(anyString(), eq(keycloakSub)))
                .thenReturn(BigDecimal.valueOf(1000));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction createdTransaction = transactionService.createIncome(keycloakSub, amount, TransactionType.INCOME,
                IncomeCategory.SALARY, "Salary");

        //THEN
        assertNotNull(createdTransaction);
        assertEquals(amount, createdTransaction.getAmount());
        verify(accountsFeignClient).updateBalance(eq("Bearer mocked-token"), any(BalanceUpdateRequest.class));
    }

    @Test
    void shouldFindTransactionsByType() {
        //GIVEN
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setTransactionType(TransactionType.INCOME);

        //WHEN
        when(transactionRepository.findByKeycloakSubAndTransactionType(keycloakSub, TransactionType.INCOME))
                .thenReturn(List.of(transaction));

        List<Transaction> transactions = transactionService.findByTransactionType(keycloakSub, TransactionType.INCOME);

        //THEN
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldFindAllTransactionsByUser() {
        //GIVEN
        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);

        //WHEN
        when(transactionRepository.findTransactionsByKeycloakSub(keycloakSub))
                .thenReturn(List.of(transaction));

        List<Transaction> transactions = transactionService.findTransactions(keycloakSub);

        //THEN
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldDeleteTransaction() {
        //GIVEN
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

        //WHEN
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.of(transaction));

        boolean deleted = transactionService.deleteTransaction(keycloakSub, 1L);

        assertTrue(deleted, "SHOULD RETURN TRUE AFTER SUCCESSFULLY DELETE.");
        verify(transactionRepository).findTransactionByKeycloakSubAndId(keycloakSub, 1L);
        verify(accountsFeignClient).updateBalanceAfterDelete(eq("Bearer mocked-token"),
                argThat(req ->
                        req.getKeycloakSub().equals(keycloakSub)
                                && req.getAmount().equals(BigDecimal.valueOf(150))
                                && req.getTransactionType().equals(TransactionType.EXPENSE)
                )
        );

        //THEN
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTransaction() {
        //GIVEN
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);

        //WHEN
        when(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(keycloakSub, 1L));

        //THEN
        verify(transactionRepository, never()).delete(any(Transaction.class));
        verify(accountsFeignClient, never()).updateBalanceAfterDelete(anyString(), any(BalanceUpdateRequest.class));
    }
}
