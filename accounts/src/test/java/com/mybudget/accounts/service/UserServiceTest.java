package com.mybudget.accounts.service;

import com.mybudget.accounts.common.CurrentUserProvider;
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
import org.springframework.kafka.core.KafkaTemplate;

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

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CurrentUserProvider currentUserProvider;

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
        when(currentUserProvider.getKeycloakSub())
                .thenReturn(testUserWithoutBalance.getKeycloakSub());
        when(userRepository.findByKeycloakSub(testUserWithoutBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithoutBalance));

        // WHEN
        userService.setBalance(newBalance);

        // THEN
        assertEquals(newBalance, testUserWithoutBalance.getBalance());
        verify(userRepository).save(testUserWithoutBalance);
    }

    @Test
    public void shouldNotSetBalanceWhenAlreadySet() {
        // GIVEN
        BigDecimal newBalance = BigDecimal.valueOf(20.00);
        when(currentUserProvider.getKeycloakSub())
                .thenReturn(testUserWithBalance.getKeycloakSub());
        when(userRepository.findByKeycloakSub(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));

        // WHEN
        // THEN
        assertThrows(BalanceAlreadySetException.class, () ->
                userService.setBalance(newBalance)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldDeleteUser() {
        // GIVEN
        when(userRepository.findByUsername(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));
        // WHEN
        userService.deleteUserCategoriesAndTransactions(testUserWithBalance.getKeycloakSub());
        // THEN
        verify(kafkaTemplate).send(anyString(), any());
        verify(userRepository).delete(testUserWithBalance);
    }

    @Test
    void shouldHandleZeroAmountIncomeAndSave() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithoutBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithoutBalance));
        // WHEN
        BigDecimal result = userService.updateBalance(
                testUserWithoutBalance.getKeycloakSub(),
                BigDecimal.ZERO,
                "INCOME"
        );
        // THEN
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
        verify(userRepository).save(testUserWithoutBalance);
    }

    @Test
    void shouldHandleLowercaseTransactionType() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithoutBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithoutBalance));
        when(userRepository.findByKeycloakSub(testUserWithBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithBalance));
        // WHEN
        BigDecimal add = userService.updateBalance(
                testUserWithoutBalance.getKeycloakSub(),
                BigDecimal.valueOf(5),
                "income"
        );
        BigDecimal sub = userService.updateBalance(
                testUserWithBalance.getKeycloakSub(),
                BigDecimal.valueOf(3),
                "expense"
        );
        // THEN
        assertEquals(0, add.compareTo(BigDecimal.valueOf(5)));
        assertEquals(0, sub.compareTo(BigDecimal.valueOf(7)));
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void shouldThrowOnUnknownTransactionType() {
        // GIVEN
        when(userRepository.findByKeycloakSub(testUserWithoutBalance.getKeycloakSub()))
                .thenReturn(Optional.of(testUserWithoutBalance));
        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                userService.updateBalance(
                        testUserWithoutBalance.getKeycloakSub(),
                        BigDecimal.ONE,
                        "TRANSFER"
                )
        );
        verify(userRepository, never()).save(any(User.class));
    }

}
