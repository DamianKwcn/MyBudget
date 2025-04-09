package com.mybudget.transactions.listener;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.TransactionConfirmEvent;
import com.mybudget.common.event.TransactionRollbackEvent;
import com.mybudget.transactions.exception.InsufficientFundsException;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final TransactionRepository transactionRepository;

    @KafkaListener(topics = "transaction-confirm", groupId = "transactions-group")
    public void onTransactionConfirm(TransactionConfirmEvent event) {
        log.info("Transactions: Received TransactionConfirmEvent for sub={}, transactionId={}",
                event.getKeycloakSub(),event.getTransactionId());

        if (event.getBalanceAfter().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Transactions: Negative balance detected for sub={}: balanceAfter={}",
                    event.getKeycloakSub(), event.getBalanceAfter());
            throw new InsufficientFundsException(event.getKeycloakSub());
        }

        Optional<Transaction> optTransaction =
                transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                        event.getKeycloakSub(), TransactionStatus.PENDING);
        if (optTransaction.isEmpty()) {
            log.warn("Transactions: Could not find a pending transaction for sub={}, transactionId={}",
                    event.getKeycloakSub(),event.getTransactionId());
            return;
        }

        Transaction transaction = optTransaction.get();
        transaction.setBalanceAfter(event.getBalanceAfter());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transactions: Transaction ID={} for sub={} has been CONFIRMED with Status={}",
                transaction.getId(), transaction.getKeycloakSub(), transaction.getStatus());
    }

    @KafkaListener(topics = "transaction-rollback", groupId = "transactions-group")
    public void onTransactionRollback(TransactionRollbackEvent event) {
        log.warn("Transactions: Received TransactionRollbackEvent for sub={}", event.getKeycloakSub());

        Optional<Transaction> optTransaction = transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                event.getKeycloakSub(), TransactionStatus.PENDING);
        if (optTransaction.isEmpty()) {
            log.warn("Transactions: Could not find a pending transaction for sub={}, so skipping rollback", event.getKeycloakSub());
            return;
        }

        Transaction transaction = optTransaction.get();
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.delete(transaction);

        log.info("Transactions: Transaction ID={} for sub={} has been rolled back",
                transaction.getId(), transaction.getKeycloakSub());

        throw new InsufficientFundsException(transaction.getKeycloakSub());
    }
}
