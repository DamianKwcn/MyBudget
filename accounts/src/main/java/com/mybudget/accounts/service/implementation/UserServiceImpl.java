package com.mybudget.accounts.service.implementation;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.exception.BalanceAlreadySetException;
import com.mybudget.accounts.exception.ResourceNotFoundException;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.UserService;
import com.mybudget.common.event.TransactionsAfterUserDeleteEvent;
import com.mybudget.common.event.UserCreatedEvent;
import com.mybudget.common.kafka.Topics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.mybudget.common.kafka.Topics.QUEUING_USERS_DELETE_V1;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public User findUserByKeycloakSub(String keycloakSub) {
        logger.debug("Searching for user with keycloakSub={}", keycloakSub);
        return userRepository.findByKeycloakSub(keycloakSub)
                .orElseThrow(() -> {
                    logger.warn("User not found with keycloakSub={}", keycloakSub);
                    return new ResourceNotFoundException("User", "keycloakSub", keycloakSub);
                });
    }

    @Transactional
    @Override
    public void createUser(String keycloakSub, String email, String username) {
        logger.info("Creating user with keycloakSub={}, email={}, username={}", keycloakSub, email, username);

        if (userRepository.findByKeycloakSub(keycloakSub).isPresent()) {
            logger.warn("User with keycloakSub={} already exists. Creation aborted.", keycloakSub);
            return;
        }

        User user = new User();
        user.setKeycloakSub(keycloakSub);
        user.setEmail(email);
        user.setUsername(username);
        user.setBalance(BigDecimal.ZERO);
        userRepository.save(user);

        kafkaTemplate.send(
                Topics.STREAMING_USERS_CREATED_V1,
                new UserCreatedEvent(keycloakSub, username)
        );

        logger.info("Published UserCreatedEvent for sub={}, username={}", keycloakSub, username);
        logger.info("User successfully created with keycloakSub={}", keycloakSub);
    }

    @Transactional
    @Override
    public void setBalance(String keycloakSub, BigDecimal balance) {
        logger.info("Setting balance for user with keycloakSub={} to newBalance={}", keycloakSub, balance);

        User user = findUserByKeycloakSub(keycloakSub);

        if (user.getBalance() != null && user.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            logger.warn("Balance already set for user with keycloakSub={}, currentBalance={}",
                    keycloakSub, user.getBalance());
            throw new BalanceAlreadySetException("Balance", "keycloakSub", keycloakSub);
        }

        user.setBalance(balance);
        userRepository.save(user);
        logger.info("Balance successfully set for user with keycloakSub={}, newBalance={}",
                keycloakSub, balance);
    }

    @Transactional
    @Override
    public void deleteUserCategoriesAndTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        TransactionsAfterUserDeleteEvent event = new TransactionsAfterUserDeleteEvent(
                username
        );

        kafkaTemplate.send(QUEUING_USERS_DELETE_V1, event);

        logger.info("Published TransactionsAfterUserDeleteEvent to user-deletion-commands for {}", username);
        userRepository.delete(user);
    }

    @Transactional
    @Override
    public BigDecimal updateBalance(String keycloakSub, BigDecimal amount, String transactionType) {
        logger.info("Updating balance for user with keycloakSub={}, amount={}, transactionType={}",
                keycloakSub, amount, transactionType);

        User user = findUserByKeycloakSub(keycloakSub);

        BigDecimal oldBalance = user.getBalance();
        BigDecimal newBalance;

        switch (transactionType.toUpperCase()) {
            case "INCOME":
                newBalance = oldBalance.add(amount);
                break;
            case "EXPENSE":
                newBalance = oldBalance.subtract(amount);
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    logger.warn("Insufficient funds for user with keycloakSub={}, currentBalance={}, attemptedWithdrawal={}",
                            keycloakSub, oldBalance, amount);
                    throw new IllegalArgumentException("Insufficient funds");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + transactionType);
        }

        user.setBalance(newBalance);
        userRepository.save(user);
        logger.info("Balance updated for user with keycloakSub={}, oldBalance={}, newBalance={}",
                keycloakSub, oldBalance, newBalance);

        return newBalance;
    }
}
