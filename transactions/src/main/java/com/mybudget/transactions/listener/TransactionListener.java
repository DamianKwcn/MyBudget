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
import com.mybudget.transactions.service.CategoryService;
import com.mybudget.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final CategoryService categoryService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    @KafkaListener(topics = STREAMING_TRANSACTIONS_CONFIRMED_V1, groupId = "transactions-group")
    public void onTransactionConfirm(TransactionConfirmEvent event) {
        logger.info("Transactions: Received TransactionConfirmEvent for sub={}, transactionId={}",
                event.getKeycloakSub(),event.getTransactionId());

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
                    event.getKeycloakSub(),event.getTransactionId());
            return;
        }

        Transaction transaction = optTransaction.get();
        transaction.setBalanceAfter(event.getBalanceAfter());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        logger.info("Transactions: Transaction ID={} for sub={} has been CONFIRMED with Status={}",
                transaction.getId(), transaction.getKeycloakSub(), transaction.getStatus());
    }

    @KafkaListener(topics = STREAMING_TRANSACTIONS_ROLLED_BACK_V1, groupId = "transactions-group")
    public void onTransactionRollback(TransactionRollbackEvent event) {
        logger.warn("Transactions: Received TransactionRollbackEvent for sub={}", event.getKeycloakSub());

        Optional<Transaction> optTransaction = transactionRepository.findFirstByKeycloakSubAndStatusOrderByIdAsc(
                event.getKeycloakSub(), TransactionStatus.PENDING);
        if (optTransaction.isEmpty()) {
            logger.warn("Transactions: Could not find a pending transaction for sub={}, so skipping rollback", event.getKeycloakSub());
            return;
        }

        Transaction transaction = optTransaction.get();
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.delete(transaction);

        logger.info("Transactions: Transaction ID={} for sub={} has been rolled back",
                transaction.getId(), transaction.getKeycloakSub());

        throw new InsufficientFundsException(transaction.getKeycloakSub());
    }

    @KafkaListener(topics = QUEUING_CATEGORIES_DELETE_V1, groupId="transactions-group")
    public void onUserDeleteTransactions(CategoriesAfterUserDeleteEvent event) {
        logger.info("Transactions: delete all for user={}", event.getUsername());
        categoryService.deleteAllByUsernameAfterDeletingAccount(event.getUsername());
    }

}
