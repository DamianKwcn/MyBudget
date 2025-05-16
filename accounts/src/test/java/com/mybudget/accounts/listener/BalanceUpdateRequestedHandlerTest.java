package com.mybudget.accounts.listener;

import com.mybudget.accounts.listener.Event.BalanceUpdateRequestedHandler;
import com.mybudget.accounts.listener.Event.BalanceUpdateResultPublisher;
import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.accounts.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceUpdateRequestedHandlerTest {

    @Mock
    private UserService userService;

    @Mock
    private BalanceUpdateResultPublisher publisher;

    @InjectMocks
    private BalanceUpdateRequestedHandler handler;

    private final BalanceUpdateRequestedEvent event =
            new BalanceUpdateRequestedEvent("sub", 1L, BigDecimal.valueOf(5), "EXPENSE");

    @Test
    void shouldHandleSuccess() {
        // GIVEN
        when(userService.updateBalance("sub", BigDecimal.valueOf(5), "EXPENSE"))
                .thenReturn(BigDecimal.valueOf(3));

        // WHEN
        handler.handle(event);

        // THEN
        verify(publisher).publish(event, TransactionStatus.SUCCESS, BigDecimal.valueOf(3));
    }

    @Test
    void shouldHandleIllegalArgumentAsFailed() {
        // GIVEN
        when(userService.updateBalance("sub", BigDecimal.valueOf(5), "EXPENSE"))
                .thenThrow(new IllegalArgumentException("Insufficient funds"));

        // WHEN
        handler.handle(event);

        // THEN
        verify(publisher).publish(event, TransactionStatus.FAILED, null);
    }

    @Test
    void shouldHandleGenericExceptionAsFailed() {
        // GIVEN
        when(userService.updateBalance(anyString(), any(), anyString()))
                .thenThrow(new RuntimeException("Unexpected"));

        // WHEN
        handler.handle(event);

        // THEN
        verify(publisher).publish(event, TransactionStatus.FAILED, null);
    }

    @Test
    void shouldHandleNullUpdatedBalanceAsSuccess() {
        // GIVEN
        when(userService.updateBalance("sub", BigDecimal.valueOf(5), "EXPENSE"))
                .thenReturn(null);

        // WHEN
        handler.handle(event);

        // THEN
        verify(publisher).publish(event, TransactionStatus.SUCCESS, null);
    }
}
