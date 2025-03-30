package com.mybudget.orchestrator.listener;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "orchestrator-commands", groupId = "orchestrator-group")
    public void onTransactionSagaStart(TransactionSagaStartEvent event) {
        log.info("Orchestrator: Received TransactionSagaStartEvent [transactionID={}, sub={}, amount={}, type={}]",
                event.getTransactionId(), event.getKeycloakSub(), event.getAmount(), event.getTransactionType());

        BalanceUpdateRequestedEvent cmd = new BalanceUpdateRequestedEvent(
                event.getKeycloakSub(),
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionType()
        );
        kafkaTemplate.send("balance-update-requests", cmd);
        log.info("Orchestrator: Sent BalanceUpdateRequestedEvent to Accounts [sub={},transactionId={} amount={}, type={}]",
                event.getKeycloakSub(),event.getTransactionId(), event.getAmount(), event.getTransactionType());
    }


    @KafkaListener(topics = "balance-update-result", groupId = "orchestrator-group")
    public void onBalanceUpdateResult(BalanceUpdateResultEvent event) {
        log.info("Orchestrator: balance update result: sub={} ,balance_after={}, amount={}, type={}, status={}",
                event.getKeycloakSub(), event.getBalanceAfter(), event.getAmount(), event.getTransactionType(), event.getStatus());

        if (event.getStatus().equals(TransactionStatus.FAILED)) {
            TransactionRollbackEvent rollback = new TransactionRollbackEvent(event.getKeycloakSub(), event.getTransactionId(), TransactionStatus.FAILED);
            kafkaTemplate.send("transaction-rollback", rollback);
            log.warn("Orchestrator: RollbackEvent sent for sub={}", event.getKeycloakSub());
        } else {
            TransactionConfirmEvent confirmEvent = new TransactionConfirmEvent(
                    event.getTransactionId(),
                    event.getKeycloakSub(),
                    event.getBalanceAfter());
            kafkaTemplate.send("transaction-confirm", confirmEvent);
            log.info("Orchestrator: TransactionConfirmEvent sent for sub={}", event.getKeycloakSub());
        }
    }

}
