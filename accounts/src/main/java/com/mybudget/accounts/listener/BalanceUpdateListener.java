package com.mybudget.accounts.listener;

import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.accounts.listener.Event.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.mybudget.common.kafka.Topics.QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1;

@Component
@RequiredArgsConstructor
public class BalanceUpdateListener {
    private final EventHandler<BalanceUpdateRequestedEvent> handler;

    @KafkaListener(topics = QUEUING_ACCOUNTS_BALANCE_UPDATE_REQUEST_V1, groupId = "accounts-group")
    public void handleBalanceUpdateRequest(BalanceUpdateRequestedEvent event) {
        handler.handle(event);
    }
}
