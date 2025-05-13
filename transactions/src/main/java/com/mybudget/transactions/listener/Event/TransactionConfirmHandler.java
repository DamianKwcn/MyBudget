package com.mybudget.transactions.listener.Event;

import com.mybudget.common.event.transaction.TransactionConfirmEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.exception.InsufficientFundsException;
import com.mybudget.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionConfirmHandler implements EventHandler<TransactionConfirmEvent> {
    private final TransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransactionConfirmHandler.class);

    @Override
    public void handle(TransactionConfirmEvent event) {
        logger.info("Transactions: Received TransactionConfirmEvent for sub={}, transactionId={}",
                event.getKeycloakSub(), event.getTransactionId());

        if (event.getBalanceAfter().compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Transactions: Negative balance detected for sub={}: balanceAfter={}",
                    event.getKeycloakSub(), event.getBalanceAfter());
            throw new InsufficientFundsException(event.getKeycloakSub());
        }

        Optional<Transaction> optTransaction =
                transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                        event.getKeycloakSub(), TransactionStatus.PENDING);
        if (optTransaction.isEmpty()) {
            logger.warn("Transactions: Could not find a pending transaction for sub={}, transactionId={}",
                    event.getKeycloakSub(), event.getTransactionId());
            return;
        }

        Transaction transaction = optTransaction.get();
        transaction.setBalanceAfter(event.getBalanceAfter());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        logger.info("Transactions: Transaction ID={} for sub={} has been CONFIRMED with Status={}",
                transaction.getId(), transaction.getKeycloakSub(), transaction.getStatus());
    }
}