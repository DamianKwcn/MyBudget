package com.mybudget.transactions.service.implementation;

import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryServiceImpl implements TransactionQueryService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUser;

    @Override
    public Transaction findByKeycloakSubAndId(Long id) {
        return transactionRepository.findByKeycloakSubAndId(currentUser.getKeycloakSub(), id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    @Override
    public List<Transaction> findByCategory(Long categoryId) {
        return transactionRepository.findAllByKeycloakSubAndCategoryId(currentUser.getKeycloakSub(), categoryId);
    }

    @Override
    public List<Transaction> findByTransactionType(TransactionType transactionType) {
        return transactionRepository.findByKeycloakSubAndTransactionType(currentUser.getKeycloakSub(), transactionType);
    }

    @Override
    public List<Transaction> findTransactions() {
        return transactionRepository.findAllByKeycloakSub(currentUser.getKeycloakSub());
    }
}