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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Transactional
    public void deleteAllByKeycloakSub(String keycloakSub) {
        transactionRepository.deleteByKeycloakSub(keycloakSub);
    }

    @Override
    public Optional<Transaction> findTransaction(String keycloakSub, Long id) {
        return Optional.ofNullable(transactionRepository.findTransactionByKeycloakSubAndId(keycloakSub, id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id.toString())));
    }

    @Transactional
    @Override
    public Transaction createTransaction(String keycloakSub,
                                         BigDecimal amount,
                                         Long categoryId,
                                         String description,
                                         String transactionType) {

        logger.info("Creating transaction for sub: {}, amount: {}, type: {}",
                keycloakSub, amount, transactionType);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId.toString()));

        if (category.getKeycloakSub() != null && !category.getKeycloakSub().equals(keycloakSub)) {
            throw new IllegalArgumentException("Category " + category.getCategoryName() + " is not available for sub: " + keycloakSub);
        }

        TransactionType type;
        try {
            type = TransactionType.valueOf(transactionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown transactionType: {}. Defaulting to EXPENSE", transactionType);
            type = TransactionType.EXPENSE;
        }

        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setTransactionType(type);

        transactionRepository.save(transaction);
        logger.info("Transaction created for sub:{}, ID: {}, type: {}, amount: {}",
                keycloakSub, transaction.getId(), transaction.getTransactionType(), transaction.getAmount());

        TransactionSagaStartEvent event = new TransactionSagaStartEvent(
                transaction.getId(),
                keycloakSub,
                amount,
                type.name()
        );
        kafkaTemplate.send("orchestrator-commands", event);
        logger.info("SagaStartEvent published to Orchestrator with type: {}", type);

        return transaction;
    }

    @Override
    public List<Transaction> findByTransactionType(String keycloakSub, TransactionType transactionType) {
        return transactionRepository.findByKeycloakSubAndTransactionType(keycloakSub, transactionType);
    }

    @Override
    public List<Transaction> findTransactions(String keycloakSub) {
        return transactionRepository.findTransactionsByKeycloakSub(keycloakSub);
    }

    @Transactional
    @Override
    public boolean deleteTransaction(String keycloakSub, Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authHeader = "Bearer " + ((JwtAuthenticationToken) authentication).getToken().getTokenValue();

        Transaction transaction = transactionRepository
                .findTransactionByKeycloakSubAndId(keycloakSub, id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id.toString()));

        transactionRepository.delete(transaction);
        return true;
    }
}