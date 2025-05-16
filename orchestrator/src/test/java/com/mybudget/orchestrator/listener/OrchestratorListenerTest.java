package com.mybudget.orchestrator.listener;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.event.BalanceUpdateResultEvent;
import com.mybudget.common.event.category.CategoryDefaultCreateEvent;
import com.mybudget.common.event.category.CategoriesAfterUserDeleteEvent;
import com.mybudget.common.event.transaction.*;
import com.mybudget.common.event.user.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static com.mybudget.common.kafka.Topics.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrchestratorListenerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private OrchestratorListener listener;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        listener = new OrchestratorListener(kafkaTemplate);
    }

    @Test
    void onTransactionSagaStart_shouldSendBalanceUpdateRequestedEvent() {
        // GIVEN
        TransactionSagaStartEvent event =
                new TransactionSagaStartEvent(1L, "sub123", BigDecimal.valueOf(100), "INCOME");
        ArgumentCaptor<BalanceUpdateRequestedEvent> captor =
                ArgumentCaptor.forClass(BalanceUpdateRequestedEvent.class);

        // WHEN
        listener.onTransactionSagaStart(event);

        // THEN
        verify(kafkaTemplate).send(eq(QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1), captor.capture());
        BalanceUpdateRequestedEvent sent = captor.getValue();
        assertThat(sent.getKeycloakSub()).isEqualTo("sub123");
        assertThat(sent.getTransactionId()).isEqualTo(1L);
        assertThat(sent.getAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(sent.getTransactionType()).isEqualTo("INCOME");
    }

    @Test
    void onBalanceUpdateResult_shouldSendRollbackOnFailed() {
        // GIVEN
        BalanceUpdateResultEvent event = new BalanceUpdateResultEvent(
                "sub456", 2L, BigDecimal.ZERO, BigDecimal.valueOf(50), "EXPENSE", TransactionStatus.FAILED
        );
        ArgumentCaptor<TransactionRollbackEvent> captor =
                ArgumentCaptor.forClass(TransactionRollbackEvent.class);

        // WHEN
        listener.onBalanceUpdateResult(event);

        // THEN
        verify(kafkaTemplate).send(eq(STREAMING_TRANSACTIONS_ROLLED_BACK_V1), captor.capture());
        TransactionRollbackEvent sent = captor.getValue();
        assertThat(sent.getKeycloakSub()).isEqualTo("sub456");
        assertThat(sent.getTransactionId()).isEqualTo(2L);
        assertThat(sent.getTransactionStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void onBalanceUpdateResult_shouldSendConfirmOnSuccess() {
        // GIVEN
        BalanceUpdateResultEvent event = new BalanceUpdateResultEvent(
                "sub789", 3L, BigDecimal.valueOf(500), BigDecimal.valueOf(200), "INCOME", TransactionStatus.SUCCESS
        );
        ArgumentCaptor<TransactionConfirmEvent> captor =
                ArgumentCaptor.forClass(TransactionConfirmEvent.class);

        // WHEN
        listener.onBalanceUpdateResult(event);

        // THEN
        verify(kafkaTemplate).send(eq(STREAMING_TRANSACTIONS_CONFIRMED_V1), captor.capture());
        TransactionConfirmEvent sent = captor.getValue();
        assertThat(sent.getKeycloakSub()).isEqualTo("sub789");
        assertThat(sent.getTransactionId()).isEqualTo(3L);
        assertThat(sent.getBalanceAfter()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void onUserCreated_shouldSendCategoryDefaultCreateEvent() {
        // GIVEN
        UserCreatedEvent event = new UserCreatedEvent("subABC", "alice");
        ArgumentCaptor<CategoryDefaultCreateEvent> captor =
                ArgumentCaptor.forClass(CategoryDefaultCreateEvent.class);

        // WHEN
        listener.onUserCreated(event);

        // THEN
        verify(kafkaTemplate).send(eq(QUEUING_CATEGORIES_CREATE_DEFAULT_V1), captor.capture());
        CategoryDefaultCreateEvent sent = captor.getValue();
        assertThat(sent.getKeycloakSub()).isEqualTo("subABC");
        assertThat(sent.getUsername()).isEqualTo("alice");
    }

    @Test
    void onUserDelete_shouldSendCategoriesAfterUserDeleteEvent() {
        // GIVEN
        TransactionsAfterUserDeleteEvent event = new TransactionsAfterUserDeleteEvent("bob");
        ArgumentCaptor<CategoriesAfterUserDeleteEvent> captor =
                ArgumentCaptor.forClass(CategoriesAfterUserDeleteEvent.class);

        // WHEN
        listener.onUserDelete(event);

        // THEN
        verify(kafkaTemplate).send(eq(QUEUING_CATEGORIES_DELETE_V1), captor.capture());
        CategoriesAfterUserDeleteEvent sent = captor.getValue();
        assertThat(sent.getUsername()).isEqualTo("bob");
    }

    @Test
    void onTransactionRemovalStarted_shouldSendBalanceUpdateRequestedWithNegatedAmount() {
        // GIVEN
        TransactionRemovalStartedEvent event =
                new TransactionRemovalStartedEvent(4L, "subXYZ", BigDecimal.valueOf(30), "EXPENSE");
        ArgumentCaptor<BalanceUpdateRequestedEvent> captor =
                ArgumentCaptor.forClass(BalanceUpdateRequestedEvent.class);

        // WHEN
        listener.onTransactionRemovalStarted(event);

        // THEN
        verify(kafkaTemplate).send(eq(QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1), captor.capture());
        BalanceUpdateRequestedEvent sent = captor.getValue();
        assertThat(sent.getKeycloakSub()).isEqualTo("subXYZ");
        assertThat(sent.getTransactionId()).isEqualTo(4L);
        assertThat(sent.getAmount()).isEqualTo(BigDecimal.valueOf(-30));
        assertThat(sent.getTransactionType()).isEqualTo("EXPENSE");
    }
}
