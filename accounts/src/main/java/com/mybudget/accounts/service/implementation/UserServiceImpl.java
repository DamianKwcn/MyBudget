package com.mybudget.accounts.service.implementation;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.exception.BalanceAlreadySetException;
import com.mybudget.accounts.exception.ResourceNotFoundException;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User findUserByKeycloakSub(String keycloakSub) {
        return userRepository.findByKeycloakSub(keycloakSub)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakSub", keycloakSub));
    }

    @Override
    public void createUser(String keycloakSub, String email, String username) {
        User user = new User();
        user.setKeycloakSub(keycloakSub);
        user.setEmail(email);
        user.setUsername(username);
        user.setBalance(BigDecimal.ZERO);
        userRepository.save(user);
        logger.info("Created new user with keycloakSub: {}", keycloakSub);
    }

    @Override
    public void setBalance(String keycloakSub, BigDecimal balance) {
        User user = findUserByKeycloakSub(keycloakSub);
        logger.info("Attempting to set balance for user with keycloakSub: {}", keycloakSub);

        if (user.getBalance() != null && user.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            logger.warn("Balance already set for user with keycloakSub: {}", keycloakSub);
            throw new BalanceAlreadySetException("Balance already set for user with keycloakSub: " + keycloakSub);
        }
        user.setBalance(balance);
        userRepository.save(user);
        logger.info("Successfully set balance for user with keycloakSub: {}, amount: {}", keycloakSub, balance);
    }

    @Override
    public boolean deleteUser(String keycloakSub) {
        User user = findUserByKeycloakSub(keycloakSub);
        if (user == null) {
            throw new ResourceNotFoundException("User", "keycloakSub", keycloakSub);
        }
        userRepository.delete(user);
        return true;
    }
}