package com.mybudget.transactions.listener.Event;

import com.mybudget.common.event.TransactionRollbackEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.exception.InsufficientFundsException;
import com.mybudget.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionRollbackHandler implements EventHandler<TransactionRollbackEvent> {
    private final TransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransactionRollbackHandler.class);

    @Override
    public void handle(TransactionRollbackEvent event) {
        logger.warn("Transactions: Received TransactionRollbackEvent for sub={}", event.getKeycloakSub());

        Optional<Transaction> optTransaction =
                transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                        event.getKeycloakSub(), TransactionStatus.PENDING);
        if (optTransaction.isEmpty()) {
            logger.warn("Transactions: Could not find a pending transaction for sub={}, so skipping rollback",
                    event.getKeycloakSub());
            return;
        }

        Transaction transaction = optTransaction.get();
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.delete(transaction);

        logger.info("Transactions: Transaction ID={} for sub={} has been rolled back",
                transaction.getId(), transaction.getKeycloakSub());

        throw new InsufficientFundsException(transaction.getKeycloakSub());
    }
}
