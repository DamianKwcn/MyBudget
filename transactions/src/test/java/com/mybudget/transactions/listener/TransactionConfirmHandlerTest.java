package com.mybudget.transactions.listener;


import com.mybudget.common.event.transaction.TransactionConfirmEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.exception.InsufficientFundsException;
import com.mybudget.transactions.listener.Event.TransactionConfirmHandler;
import com.mybudget.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionConfirmHandlerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionConfirmHandler handler;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(42L);
        transaction.setKeycloakSub("sub");
        transaction.setStatus(TransactionStatus.PENDING);
    }

    @Test
    void shouldThrowInsufficientFundsException_whenBalanceAfterIsNegative() {
        // GIVEN
        TransactionConfirmEvent event = new TransactionConfirmEvent(42L, "sub", BigDecimal.valueOf(-10));

        // WHEN / THEN
        assertThrows(InsufficientFundsException.class, () -> handler.handle(event));
        verify(transactionRepository, never())
                .findFirstByKeycloakSubAndStatusOrderByIdAsc(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldDoNothing_whenNoPendingTransactionFound() {
        // GIVEN
        TransactionConfirmEvent event = new TransactionConfirmEvent(42L, "sub", BigDecimal.valueOf(100));
        when(transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc("sub", TransactionStatus.PENDING))
                .thenReturn(Optional.empty());

        // WHEN
        handler.handle(event);

        // THEN
        verify(transactionRepository).findFirstByKeycloakSubAndStatusOrderByIdAsc("sub", TransactionStatus.PENDING);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldConfirmTransactionAndSave_whenPendingTransactionExists() {
        // GIVEN
        TransactionConfirmEvent event = new TransactionConfirmEvent(42L, "sub", BigDecimal.valueOf(200));
        when(transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc("sub", TransactionStatus.PENDING))
                .thenReturn(Optional.of(transaction));

        // WHEN
        handler.handle(event);

        // THEN
        assertEquals(BigDecimal.valueOf(200), transaction.getBalanceAfter());
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }
}
