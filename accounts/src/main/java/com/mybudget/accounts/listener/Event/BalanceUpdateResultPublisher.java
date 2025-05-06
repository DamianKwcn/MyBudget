package com.mybudget.accounts.listener.Event;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.event.BalanceUpdateResultEvent;
import com.mybudget.common.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mybudget.common.kafka.Topics.STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1;

@Component
@RequiredArgsConstructor
public class BalanceUpdateResultPublisher {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(BalanceUpdateResultPublisher.class);

    public void publish(BalanceUpdateRequestedEvent event,
                        TransactionStatus status,
                        BigDecimal updatedBalance) {

        Optional<User> userOptional = userRepository.findByKeycloakSub(event.getKeycloakSub());
        if (userOptional.isEmpty()) {
            logger.error("Accounts: User not found for keycloakSub={}", event.getKeycloakSub());
            return;
        }

        User user = userOptional.get();
        BigDecimal finalBalance = updatedBalance != null
                ? updatedBalance
                : user.getBalance();

        BalanceUpdateResultEvent resultEvent = new BalanceUpdateResultEvent(
                event.getKeycloakSub(),
                event.getTransactionId(),
                finalBalance,
                event.getAmount(),
                event.getTransactionType(),
                status
        );

        kafkaTemplate.send(STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1, resultEvent);
        logger.info("Accounts: Published BalanceUpdateResultEvent sub={}, transactionId={}, amount={}, type={}, status={}",
                event.getKeycloakSub(),
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionType(),
                status
        );
    }
}
