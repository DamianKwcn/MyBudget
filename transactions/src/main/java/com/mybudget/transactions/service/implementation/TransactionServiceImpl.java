package com.mybudget.transactions.service.implementation;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.TransactionSagaStartEvent;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.mybudget.common.kafka.Topics.STREAMING_TRANSACTIONS_SAGA_STARTED_V1;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Transactional
    public void deleteAllByKeycloakSub(String keycloakSub) {
        if (keycloakSub == null) {
            throw new ResourceNotFoundException("KeycloakSub", "keycloakSub", "not found");
        }
        transactionRepository.deleteByKeycloakSub(keycloakSub);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction findByKeycloakSubAndId(String keycloakSub, Long id) {
        return transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, id)
                .orElseThrow(() -> new ResourceNotFoundException(keycloakSub, "Transaction", id.toString()));
    }

    @Transactional
    @Override
    public Transaction createTransaction(String keycloakSub,
                                         BigDecimal amount,
                                         Long categoryId,
                                         String description) {

        logger.info("Creating transaction for sub={}, amount={}",
                keycloakSub, amount);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "id", categoryId.toString()));

        if (category.getKeycloakSub() != null &&
                !category.getKeycloakSub().equals(keycloakSub)) {
            throw new IllegalArgumentException(
                    "Category " + category.getCategoryName() + " is not available for sub: " + keycloakSub);
        }

        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setKeycloakSub(keycloakSub);
        transaction.setTransactionType(category.getTransactionType());
        transaction.setAmount(amount);
        transaction.setDescription(description);

        category.addTransaction(transaction);
        category.setCategoryBalance(category.getCategoryBalance().add(amount));
        categoryRepository.save(category);

        logger.info("Creating transaction for sub={}, ID={}, type={}, amount={}",
                keycloakSub, transaction.getId(), transaction.getTransactionType(), transaction.getAmount());

        TransactionSagaStartEvent event = new TransactionSagaStartEvent(
                transaction.getId(),
                keycloakSub,
                amount,
                transaction.getTransactionType().name()
        );
        kafkaTemplate.send(STREAMING_TRANSACTIONS_SAGA_STARTED_V1, event);
        logger.info("SagaStartEvent published to Orchestrator with sub={}, ID={}, type={}, amount={}",
                keycloakSub, transaction.getId(), transaction.getTransactionType(), transaction.getAmount());

        return transaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findByTransactionType(String keycloakSub, TransactionType transactionType) {
        return transactionRepository.findByKeycloakSubAndTransactionType(keycloakSub, transactionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findTransactions(String keycloakSub) {
        return transactionRepository.findTransactionsByKeycloakSub(keycloakSub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findByCategory(String keycloakSub, Long categoryId) {
        return transactionRepository.findByCategoryIdAndKeycloakSub(categoryId, keycloakSub);
    }

    @Transactional
    @Override
    public void deleteTransaction(String keycloakSub, Long id) {

        Transaction transaction = transactionRepository
                .findTransactionByKeycloakSubAndId(keycloakSub, id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction", "id", id.toString()));

        Category category = transaction.getCategory();

        category.removeTransaction(transaction);
        category.setCategoryBalance(category.getCategoryBalance().subtract(transaction.getAmount()));

        categoryRepository.save(category);
    }
}