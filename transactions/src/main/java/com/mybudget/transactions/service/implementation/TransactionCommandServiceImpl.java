package com.mybudget.transactions.service.implementation;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.event.transaction.TransactionRemovalStartedEvent;
import com.mybudget.common.event.transaction.TransactionSagaStartEvent;
import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.TransactionCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.mybudget.common.kafka.Topics.STREAMING_TRANSACTIONS_REMOVAL_STARTED_V1;
import static com.mybudget.common.kafka.Topics.STREAMING_TRANSACTIONS_CREATION_STARTED_V1;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionCommandServiceImpl implements TransactionCommandService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CurrentUserProvider currentUser;
    private static final Logger logger = LoggerFactory.getLogger(TransactionCommandServiceImpl.class);

    @Transactional
    @Override
    public Transaction createTransaction(BigDecimal amount,
                                         Long categoryId,
                                         String description) {
        String keycloakSub = currentUser.getKeycloakSub();

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
        kafkaTemplate.send(STREAMING_TRANSACTIONS_CREATION_STARTED_V1, event);
        logger.info("STREAMING_TRANSACTIONS_CREATION_STARTED_V1 published to Orchestrator with sub={}, ID={}, type={}, amount={}",
                keycloakSub, transaction.getId(), transaction.getTransactionType(), transaction.getAmount());

        return transaction;
    }

    @Transactional
    @Override
    public void deleteTransaction(Long id) {
        String keycloakSub = currentUser.getKeycloakSub();
        Transaction transaction = transactionRepository
                .findByKeycloakSubAndId(currentUser.getKeycloakSub(), id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction", "id", id.toString()));

        Category category = transaction.getCategory();

        TransactionRemovalStartedEvent event = new TransactionRemovalStartedEvent(
                transaction.getId(),
                keycloakSub,
                transaction.getAmount(),
                transaction.getTransactionType().name()
        );
        kafkaTemplate.send(STREAMING_TRANSACTIONS_REMOVAL_STARTED_V1, event);
        logger.info("STREAMING_TRANSACTIONS_REMOVAL_STARTED_V1 published for sub={}, ID={}",
                keycloakSub, transaction.getId());

        category.removeTransaction(transaction);
        category.setCategoryBalance(category.getCategoryBalance().subtract(transaction.getAmount()));

        categoryRepository.save(category);
    }
}
