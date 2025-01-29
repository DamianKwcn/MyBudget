package com.mybudget.accounts.service;

import com.mybudget.accounts.entity.User;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

public interface UserService {
    User findUserByKeycloakSub(String keycloakSub);

    @Transactional
    void createUser(String keycloakSub, String email, String username);

    @Transactional
    void setBalance(String keycloakSub, BigDecimal balance);

    @Transactional
    boolean deleteUser(String keycloakSub);
}
