package com.mybudget.accounts.listener.Event;

import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.accounts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BalanceUpdateRequestedHandler implements EventHandler<BalanceUpdateRequestedEvent> {
    private final UserService userService;
    private final BalanceUpdateResultPublisher publisher;
    private static final Logger logger = LoggerFactory.getLogger(BalanceUpdateRequestedHandler.class);

    @Override
    public void handle(BalanceUpdateRequestedEvent event) {
        logger.info("Accounts: Received BalanceUpdateRequestedEvent sub={}, transactionId={}, amount={}, type={}",
                event.getKeycloakSub(),
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionType()
        );

        TransactionStatus status;
        BigDecimal updatedBalance = null;

        try {
            updatedBalance = userService.updateBalance(
                    event.getKeycloakSub(),
                    event.getAmount(),
                    event.getTransactionType()
            );
            status = TransactionStatus.SUCCESS;
        } catch (IllegalArgumentException e) {
            status = TransactionStatus.FAILED;
            logger.warn("Accounts: Balance update failed for sub={}.", event.getKeycloakSub());
        } catch (Exception e) {
            status = TransactionStatus.FAILED;
            logger.error("Accounts: Error while processing balance update request={}", e.getMessage(), e);
        }
        publisher.publish(event, status, updatedBalance);
    }
}
