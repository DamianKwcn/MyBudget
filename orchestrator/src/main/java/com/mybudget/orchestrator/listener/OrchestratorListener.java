package com.mybudget.orchestrator.listener;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.event.BalanceUpdateResultEvent;
import com.mybudget.common.event.category.CategoriesAfterUserDeleteEvent;
import com.mybudget.common.event.category.CategoryDefaultCreateEvent;
import com.mybudget.common.event.transaction.*;
import com.mybudget.common.event.user.UserCreatedEvent;
import com.mybudget.common.kafka.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.mybudget.common.kafka.Topics.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorListener {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorListener.class);

    @KafkaListener(topics = STREAMING_TRANSACTIONS_CREATION_STARTED_V1, groupId="orchestrator-group")
    public void onTransactionSagaStart(TransactionSagaStartEvent event) {
        logger.info("Orchestrator: Received TransactionSagaStartEvent transactionID={}, sub={}, amount={}, type={}",
                event.getTransactionId(), event.getKeycloakSub(), event.getAmount(), event.getTransactionType());

        BalanceUpdateRequestedEvent confirmEvent = new BalanceUpdateRequestedEvent(
                event.getKeycloakSub(),
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionType()
        );

        kafkaTemplate.send(QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1, confirmEvent);

        logger.info("Orchestrator: Sent BalanceUpdateRequestedEvent to Accounts sub={},transactionId={} amount={}, type={}",
                confirmEvent.getKeycloakSub(),confirmEvent.getTransactionId(), confirmEvent.getAmount(), confirmEvent.getTransactionType());
    }

    @KafkaListener(topics = STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1, groupId = "orchestrator-group")
    public void onBalanceUpdateResult(BalanceUpdateResultEvent event) {
        logger.info("Orchestrator: balance update result: sub={} ,balance_after={}, amount={}, type={}, status={}",
                event.getKeycloakSub(), event.getBalanceAfter(), event.getAmount(), event.getTransactionType(), event.getStatus());

        if (event.getStatus().equals(TransactionStatus.FAILED)) {
            TransactionRollbackEvent rollback = new TransactionRollbackEvent(event.getKeycloakSub(), event.getTransactionId(), TransactionStatus.FAILED);

            kafkaTemplate.send(STREAMING_TRANSACTIONS_ROLLED_BACK_V1, rollback);

            logger.warn("Orchestrator: RollbackEvent sent for sub={}", event.getKeycloakSub());
        } else {
            TransactionConfirmEvent confirmEvent = new TransactionConfirmEvent(
                    event.getTransactionId(),
                    event.getKeycloakSub(),
                    event.getBalanceAfter());

            kafkaTemplate.send(STREAMING_TRANSACTIONS_CONFIRMED_V1, confirmEvent);

            logger.info("Orchestrator: TransactionConfirmEvent sent for sub={}, transactionID={}, amount={},balanceAfter={} type={}",
                    event.getKeycloakSub(), event.getTransactionId(), event.getAmount(),event.getBalanceAfter(), event.getTransactionId());
        }
    }

    @KafkaListener(topics = STREAMING_USERS_CREATED_V1, groupId = "orchestrator-group")
    public void onUserCreated(UserCreatedEvent event) {
        logger.info("Orchestrator: received UserCreatedEvent sub={}, username={}",
                event.getKeycloakSub(), event.getUsername());

        CategoryDefaultCreateEvent confirmEvent =
                new CategoryDefaultCreateEvent(event.getKeycloakSub(), event.getUsername());

        kafkaTemplate.send(QUEUING_CATEGORIES_CREATE_DEFAULT_V1, confirmEvent);

        logger.info("Orchestrator: sent CategoryDefaultCreateEvent for sub={}, username={}",
                event.getKeycloakSub(), event.getUsername());
    }

    @KafkaListener(topics = QUEUING_USERS_DELETE_V1, groupId="orchestrator-group")
    public void onUserDelete(TransactionsAfterUserDeleteEvent event) {
        logger.info("Orchestrator: UserDelete event send with username={}", event.getUsername());
        kafkaTemplate.send(QUEUING_CATEGORIES_DELETE_V1, new CategoriesAfterUserDeleteEvent(event.getUsername()));
    }

    @KafkaListener(topics = STREAMING_TRANSACTIONS_REMOVAL_STARTED_V1, groupId = "orchestrator-group")
    public void onTransactionRemovalStarted(TransactionRemovalStartedEvent event) {
        logger.info("Orchestrator: Received TransactionRemovalStartedEvent id={}, sub={}, amount={}, type={}",
                event.getTransactionId(), event.getKeycloakSub(), event.getAmount(), event.getTransactionType());

        BalanceUpdateRequestedEvent confirmEvent = new BalanceUpdateRequestedEvent(
                event.getKeycloakSub(),
                event.getTransactionId(),
                event.getAmount().negate(),
                event.getTransactionType()
        );

        kafkaTemplate.send(QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1, confirmEvent);

        logger.info("Orchestrator: Sent BalanceUpdateRequestedEvent to Accounts sub={},transactionId={} amount={}, type={}",
                confirmEvent.getKeycloakSub(),confirmEvent.getTransactionId(), confirmEvent.getAmount(), confirmEvent.getTransactionType());
    }


}
