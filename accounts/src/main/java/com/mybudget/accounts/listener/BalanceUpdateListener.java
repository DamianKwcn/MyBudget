package com.mybudget.accounts.listener;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.service.UserService;
import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.event.BalanceUpdateResultEvent;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.common.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceUpdateListener {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserService userService;

    @KafkaListener(topics = "balance-update-requests", groupId = "accounts-group")
    public void handleBalanceUpdateRequest(BalanceUpdateRequestedEvent event) {
        log.info("Accounts: Received BalanceUpdateRequestedEvent sub={}, transactionId={}, amount={}, type={}",
                event.getKeycloakSub(),event.getTransactionId(), event.getAmount(), event.getTransactionType());

        try {
            BigDecimal updatedBalance = userService.updateBalance(
                    event.getKeycloakSub(),
                    event.getAmount(),
                    event.getTransactionType()
            );

            publishBalanceUpdateResult(event, TransactionStatus.SUCCESS, updatedBalance);

        } catch (IllegalArgumentException ex) {
            publishBalanceUpdateResult(event, TransactionStatus.FAILED, null);
            log.warn("Accounts: Transaction failed for sub={} due to insufficient funds or unknown transaction type.", event.getKeycloakSub());
        } catch (Exception e) {
            log.error("Accounts: Error while processing balance update request={}", e.getMessage(), e);
            publishBalanceUpdateResult(event, TransactionStatus.FAILED, null);
        }
    }

    private void publishBalanceUpdateResult(BalanceUpdateRequestedEvent event, TransactionStatus status, BigDecimal updatedBalance) {
        Optional<User> userOptional = userRepository.findByKeycloakSub(event.getKeycloakSub());

        if (userOptional.isEmpty()) {
            log.error("Accounts: User not found for keycloakSub={}", event.getKeycloakSub());
            return;
        }

        User user = userOptional.get();

        BalanceUpdateResultEvent resultEvent = new BalanceUpdateResultEvent(
                event.getKeycloakSub(),
                event.getTransactionId(),
                updatedBalance != null ? updatedBalance : user.getBalance(),
                event.getAmount(),
                event.getTransactionType(),
                status
        );

        kafkaTemplate.send("balance-update-result", resultEvent);

        log.info("Accounts: Published BalanceUpdateResultEvent sub={}, transactionId={}, amount={}, type={}, status={}",
                event.getKeycloakSub(), event.getTransactionId(), event.getAmount(), event.getTransactionType(), status);
    }
}
