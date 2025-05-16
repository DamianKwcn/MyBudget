package com.mybudget.transactions.listener;

import com.mybudget.common.event.transaction.TransactionRollbackEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.exception.InsufficientFundsException;
import com.mybudget.transactions.listener.Event.TransactionRollbackHandler;
import com.mybudget.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRollbackHandlerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionRollbackHandler handler;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(100L);
        transaction.setKeycloakSub("sub");
        transaction.setStatus(TransactionStatus.PENDING);
    }

    @Test
    void shouldDoNothing_whenNoPendingTransactionFound() {
        // GIVEN
        TransactionRollbackEvent event =
                new TransactionRollbackEvent("sub", 100L, TransactionStatus.FAILED);
        when(transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                "sub", TransactionStatus.PENDING))
                .thenReturn(Optional.empty());

        // WHEN
        handler.handle(event);

        // THEN
        verify(transactionRepository)
                .findFirstByKeycloakSubAndStatusOrderByIdAsc("sub", TransactionStatus.PENDING);
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteTransactionAndThrowInsufficientFundsException_whenPendingTransactionFound() {
        // GIVEN
        TransactionRollbackEvent event =
                new TransactionRollbackEvent("sub", 100L, TransactionStatus.FAILED);
        when(transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                "sub", TransactionStatus.PENDING))
                .thenReturn(Optional.of(transaction));

        // WHEN / THEN
        InsufficientFundsException ex = assertThrows(
                InsufficientFundsException.class,
                () -> handler.handle(event)
        );

        // THEN
        assertEquals(
                "Insufficient funds for user with keycloakSub: sub",
                ex.getMessage()
        );
        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(transactionRepository).delete(transaction);
    }
}
