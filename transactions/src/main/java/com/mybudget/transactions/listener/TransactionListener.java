package com.mybudget.transactions.listener;

import com.mybudget.common.event.TransactionConfirmEvent;
import com.mybudget.common.event.TransactionRollbackEvent;
import com.mybudget.common.event.CategoriesAfterUserDeleteEvent;
import com.mybudget.transactions.listener.Event.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.mybudget.common.kafka.Topics.*;

@Component
@RequiredArgsConstructor
public class TransactionListener {
    private final EventHandler<TransactionConfirmEvent> confirmHandler;
    private final EventHandler<TransactionRollbackEvent> rollbackHandler;
    private final EventHandler<CategoriesAfterUserDeleteEvent> userDeleteHandler;

    @KafkaListener(topics = STREAMING_TRANSACTIONS_CONFIRMED_V1, groupId = "transactions-group")
    public void onTransactionConfirm(TransactionConfirmEvent event) {
        confirmHandler.handle(event);
    }

    @KafkaListener(topics = STREAMING_TRANSACTIONS_ROLLED_BACK_V1, groupId = "transactions-group")
    public void onTransactionRollback(TransactionRollbackEvent event) {
        rollbackHandler.handle(event);
    }

    @KafkaListener(topics = QUEUING_CATEGORIES_DELETE_V1, groupId = "transactions-group")
    public void onUserDeleteTransactions(CategoriesAfterUserDeleteEvent event) {
        userDeleteHandler.handle(event);
    }
}
