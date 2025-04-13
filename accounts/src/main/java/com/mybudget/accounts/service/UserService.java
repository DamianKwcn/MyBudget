package com.mybudget.accounts.service;

import com.mybudget.accounts.entity.User;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

public interface UserService {
    User findUserByKeycloakSub(String keycloakSub);

    void createUser(String keycloakSub, String email, String username);

    void setBalance(String keycloakSub, BigDecimal balance);

    void deleteUserAndTransactions(String username);

    BigDecimal updateBalance(String keycloakSub, BigDecimal amount, String transactionType);
}
