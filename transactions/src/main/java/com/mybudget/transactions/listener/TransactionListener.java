package com.mybudget.transactions.listener;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.CategoriesAfterUserDeleteEvent;
import com.mybudget.common.event.TransactionConfirmEvent;
import com.mybudget.common.event.TransactionRollbackEvent;
import com.mybudget.common.event.TransactionsAfterUserDeleteEvent;
import com.mybudget.transactions.exception.InsufficientFundsException;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mybudget.common.kafka.Topics.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionService transactionService;

    @KafkaListener(topics = STREAMING_TRANSACTIONS_CONFIRMED_V1, groupId = "transactions-group")
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

    @KafkaListener(topics = STREAMING_TRANSACTIONS_ROLLED_BACK_V1, groupId = "transactions-group")
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

    @KafkaListener(topics = QUEUING_TRANSACTIONS_DELETE_V1, groupId="transactions-group")
    public void onUserDeleteTransactions(TransactionsAfterUserDeleteEvent event) {
        log.info("Transactions: delete all for user={}", event.getUsername());
        transactionService.deleteAllByUsername(event.getUsername());
    }

}
