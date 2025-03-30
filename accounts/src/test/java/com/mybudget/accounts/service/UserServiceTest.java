package com.mybudget.accounts.service;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.exception.BalanceAlreadySetException;
import com.mybudget.accounts.exception.ResourceNotFoundException;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUserWithBalance;
    private User testUserWithoutBalance;

    @BeforeEach
    public void setUp() {
        testUserWithBalance = new User();
        testUserWithBalance.setKeycloakSub("testSubWithBalance");
        testUserWithBalance.setUsername("test-user-with-balance");
        testUserWithBalance.setEmail("withbalance@example.com");
        testUserWithBalance.setBalance(BigDecimal.TEN);

        testUserWithoutBalance = new User();
        testUserWithoutBalance.setKeycloakSub("testSubWithoutBalance");
        testUserWithoutBalance.setUsername("test-user-without-balance");
        testUserWithoutBalance.setEmail("withoutbalance@example.com");
        testUserWithoutBalance.setBalance(BigDecimal.ZERO);
    }

    @Test
    public void shouldFindUserByKeycloakSub() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));

        // WHEN
        User foundUser = userService.findUserByKeycloakSub(testUserWithBalance.getKeycloakSub());

        // THEN
        assertNotNull(foundUser);
        assertEquals(testUserWithBalance.getKeycloakSub(), foundUser.getKeycloakSub());
        verify(userRepository, times(1)).findByKeycloakSub(testUserWithBalance.getKeycloakSub());
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFound() {
        // GIVEN
        when(userRepository.findByKeycloakSub("nonExistentSub")).thenReturn(Optional.empty());

        // THEN
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findUserByKeycloakSub("nonExistentSub");
        });

        verify(userRepository, times(1)).findByKeycloakSub("nonExistentSub");
    }

    @Test
    public void shouldCreateUserIfNotExists() {
        // GIVEN
        String keycloakSub = "newUserSub";
        String email = "newuser@example.com";
        String username = "newuser";

        when(userRepository.findByKeycloakSub(keycloakSub)).thenReturn(Optional.empty());

        // WHEN
        userService.createUser(keycloakSub, email, username);

        // THEN
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldNotCreateUserIfAlreadyExists() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));

        // WHEN
        userService.createUser(testUserWithBalance.getKeycloakSub(), "test@example.com", "test-user");

        // THEN
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldSetBalanceWhenNotAlreadySet() {
        // GIVEN
        BigDecimal newBalance = BigDecimal.valueOf(10.00);
        when(userRepository.findByKeycloakSub(testUserWithoutBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithoutBalance));

        // WHEN
        userService.setBalance(testUserWithoutBalance.getKeycloakSub(), newBalance);

        // THEN
        assertEquals(newBalance, testUserWithoutBalance.getBalance());
        verify(userRepository, times(1)).save(testUserWithoutBalance);
    }

    @Test
    public void shouldNotSetBalanceWhenAlreadySet() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));

        // WHEN
        BigDecimal newBalance = BigDecimal.valueOf(20.00);

        // THEN
        assertThrows(BalanceAlreadySetException.class, () -> {
            userService.setBalance(testUserWithBalance.getKeycloakSub(), newBalance);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldDeleteUser() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));

        // WHEN
        userService.deleteUserAndTransactions(testUserWithBalance.getKeycloakSub());

        // THEN
        verify(userRepository, times(1)).delete(testUserWithBalance);
    }


}
