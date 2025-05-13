package com.mybudget.orchestrator.listener;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.*;
import com.mybudget.common.event.transaction.TransactionConfirmEvent;
import com.mybudget.common.event.transaction.TransactionRollbackEvent;
import com.mybudget.common.event.transaction.TransactionSagaStartEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrchestratorListenerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private OrchestratorListener orchestratorListener;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        orchestratorListener = new OrchestratorListener(kafkaTemplate);
    }

    @Test
    void shouldSendBalanceUpdateRequestedEvent_whenSagaStarts() {
        // given
        TransactionSagaStartEvent event = new TransactionSagaStartEvent(1L, "sub123", BigDecimal.valueOf(100), "INCOME");

        // when
        orchestratorListener.onTransactionSagaStart(event);

        // then
        ArgumentCaptor<BalanceUpdateRequestedEvent> captor = ArgumentCaptor.forClass(BalanceUpdateRequestedEvent.class);
        verify(kafkaTemplate).send(eq("balance-update-requests"), captor.capture());

        BalanceUpdateRequestedEvent sentEvent = captor.getValue();
        assertThat(sentEvent.getKeycloakSub()).isEqualTo("sub123");
        assertThat(sentEvent.getAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(sentEvent.getTransactionType()).isEqualTo("INCOME");
    }

    @Test
    void shouldSendTransactionRollbackEvent_whenBalanceUpdateFails() {
        // given
        BalanceUpdateResultEvent event = new BalanceUpdateResultEvent("sub456", 2L,
                BigDecimal.ZERO, BigDecimal.valueOf(100), "EXPENSE", TransactionStatus.FAILED);

        // when
        orchestratorListener.onBalanceUpdateResult(event);

        // then
        ArgumentCaptor<TransactionRollbackEvent> captor = ArgumentCaptor.forClass(TransactionRollbackEvent.class);
        verify(kafkaTemplate).send(eq("transaction-rollback"), captor.capture());

        TransactionRollbackEvent rollback = captor.getValue();
        assertThat(rollback.getKeycloakSub()).isEqualTo("sub456");
        assertThat(rollback.getTransactionId()).isEqualTo(2L);
        assertThat(rollback.getTransactionStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void shouldSendTransactionConfirmEvent_whenBalanceUpdateSucceeds() {
        // given
        BalanceUpdateResultEvent event = new BalanceUpdateResultEvent("sub789", 3L,
                BigDecimal.valueOf(500), BigDecimal.valueOf(100), "INCOME", TransactionStatus.SUCCESS);

        // when
        orchestratorListener.onBalanceUpdateResult(event);

        // then
        ArgumentCaptor<TransactionConfirmEvent> captor = ArgumentCaptor.forClass(TransactionConfirmEvent.class);
        verify(kafkaTemplate).send(eq("transaction-confirm"), captor.capture());

        TransactionConfirmEvent confirm = captor.getValue();
        assertThat(confirm.getKeycloakSub()).isEqualTo("sub789");
        assertThat(confirm.getTransactionId()).isEqualTo(3L);
        assertThat(confirm.getBalanceAfter()).isEqualTo(BigDecimal.valueOf(500));
    }
}
