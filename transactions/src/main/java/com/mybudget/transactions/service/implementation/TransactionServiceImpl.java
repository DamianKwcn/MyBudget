package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.entity.feign.BalanceUpdateRequest;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.TransactionService;
import com.mybudget.transactions.service.client.AccountsFeignClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final AccountsFeignClient accountsFeignClient;

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
    public Transaction createTransaction(String keycloakSub, BigDecimal amount, Long categoryId, String description) {
        logger.info("Creating transaction for user: {}, amount: {}", keycloakSub, amount);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId.toString()));

        if (category.getKeycloakSub() != null && !category.getKeycloakSub().equals(keycloakSub)) {
            throw new IllegalArgumentException("Category " + category.getCategoryName() +  " is not available!!!");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authHeader = "Bearer " + ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
        BigDecimal currentBalance = accountsFeignClient.getUserBalance(authHeader, keycloakSub);

        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(category.getTransactionType());
        transaction.setCategory(category);
        transaction.setDescription(description);

        if (category.getTransactionType() == TransactionType.EXPENSE) {
            transaction.setBalanceAfter(currentBalance.subtract(amount));
        } else {
            transaction.setBalanceAfter(currentBalance.add(amount));
        }

        transactionRepository.save(transaction);
        logger.info("Transaction created: {}, new balance: {}", transaction, transaction.getBalanceAfter());

        accountsFeignClient.updateBalance(authHeader, new BalanceUpdateRequest(keycloakSub, amount, category.getTransactionType()));
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

        accountsFeignClient.updateBalanceAfterDelete(authHeader, new BalanceUpdateRequest(keycloakSub, transaction.getAmount(), transaction.getTransactionType()));

        transactionRepository.delete(transaction);
        return true;
    }
}