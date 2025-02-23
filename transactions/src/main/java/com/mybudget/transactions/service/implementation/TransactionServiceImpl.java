package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.ExpenseCategory;
import com.mybudget.transactions.entity.enums.IncomeCategory;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.entity.feign.BalanceUpdateRequest;
import com.mybudget.transactions.exception.ResourceNotFoundException;
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
    public Transaction createExpense(String keycloakSub, BigDecimal amount, TransactionType transactionType,
                                     ExpenseCategory expenseCategory, String description) {
        logger.info("Creating expense for user: {}, amount: {}", keycloakSub, amount);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authHeader = "Bearer " + ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
        BigDecimal currentBalance = accountsFeignClient.getUserBalance(authHeader, keycloakSub);

        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setExpenseCategory(expenseCategory);
        transaction.setDescription(description);
        transaction.setBalanceAfter(currentBalance.subtract(amount));
        transactionRepository.save(transaction);

        logger.info("Expense created: {}, balance after: {}", transaction, transaction.getBalanceAfter());

        accountsFeignClient.updateBalance(authHeader, new BalanceUpdateRequest(keycloakSub, amount, transactionType));
        return transaction;
    }

    @Transactional
    @Override
    public Transaction createIncome(String keycloakSub, BigDecimal amount, TransactionType transactionType,
                                    IncomeCategory incomeCategory, String description) {
        logger.info("Creating income for user: {}, amount: {}", keycloakSub, amount);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authHeader = "Bearer " + ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
        BigDecimal currentBalance = accountsFeignClient.getUserBalance(authHeader, keycloakSub);

        Transaction transaction = new Transaction();
        transaction.setKeycloakSub(keycloakSub);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setIncomeCategory(incomeCategory);
        transaction.setDescription(description);
        transaction.setBalanceAfter(currentBalance.add(amount));
        transactionRepository.save(transaction);

        logger.debug("Income created: {}, balance after: {}", transaction, transaction.getBalanceAfter());

        accountsFeignClient.updateBalance(authHeader, new BalanceUpdateRequest(keycloakSub, amount, transactionType));
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
