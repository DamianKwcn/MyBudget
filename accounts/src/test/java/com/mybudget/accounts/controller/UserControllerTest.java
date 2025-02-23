package com.mybudget.accounts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.accounts.constants.UserConstants;
import com.mybudget.accounts.dto.BalanceDto;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.exception.BalanceAlreadySetException;
import com.mybudget.accounts.exception.ResourceNotFoundException;
import com.mybudget.accounts.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/users - getUserProfile()")
    class GetUserProfileTests {

        @Test
        void shouldReturnUserProfile() throws Exception {
            // GIVEN
            String keycloakSub = "test-subject";
            User user = new User();
            user.setId(1L);
            user.setKeycloakSub(keycloakSub);
            user.setUsername("test-user");
            user.setEmail("test@example.com");
            user.setBalance(BigDecimal.TEN);

            BDDMockito.given(userService.findUserByKeycloakSub(keycloakSub)).willReturn(user);

            // WHEN
            var resultActions = mockMvc.perform(get("/api/users")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", keycloakSub))));

            // THEN
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("test-user"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.balance").value(10));

            verify(userService, times(1)).findUserByKeycloakSub(keycloakSub);
        }

        @Test
        void shouldThrowResourceNotFoundException() throws Exception {
            // GIVEN
            String subject = "unknown-subject";
            BDDMockito.given(userService.findUserByKeycloakSub(subject))
                    .willThrow(new ResourceNotFoundException("User", "keycloakSub", subject));

            // WHEN
            var resultActions = mockMvc.perform(get("/api/users")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", subject))));

            // THEN
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString(
                            "User not found with the given input data keycloakSub : 'unknown-subject'"
                    )));
        }
    }

    @Nested
    @DisplayName("POST /api/users/balance - setUserBalance()")
    class SetUserBalanceTests {

        @Test
        void shouldSetBalance() throws Exception {
            // GIVEN
            String subject = "test-subject";
            BalanceDto balanceDto = new BalanceDto();
            balanceDto.setBalance(BigDecimal.valueOf(1000.00));
            doNothing().when(userService).setBalance(eq(subject), eq(balanceDto.getBalance()));

            // WHEN
            var resultActions = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", subject)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(balanceDto)));

            // THEN
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(UserConstants.STATUS_200))
                    .andExpect(jsonPath("$.statusMsg").value(UserConstants.MESSAGE_200));

            verify(userService, times(1)).setBalance(subject, BigDecimal.valueOf(1000.00));
        }

        @Test
        void shouldThrowBalanceAlreadySetException() throws Exception {
            // GIVEN
            String subject = "test-subject";
            BalanceDto balanceDto = new BalanceDto();
            balanceDto.setBalance(BigDecimal.valueOf(1000.00));
            doThrow(new BalanceAlreadySetException("Balance", "keycloakSub", subject))
                    .when(userService)
                    .setBalance(eq(subject), eq(balanceDto.getBalance()));

            // WHEN
            var resultActions = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", subject)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(balanceDto)));

            // THEN
            resultActions
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().string(containsString("Balance already set for user")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users - deleteUser()")
    class DeleteUserTests {

        @Test
        void shouldDeleteUser() throws Exception {
            // GIVEN
            String subject = "test-subject";
            doNothing().when(userService).deleteUserAndTransactions(subject);

            // WHEN
            var resultActions = mockMvc.perform(
                    delete("/api/users")
                            .with(SecurityMockMvcRequestPostProcessors.jwt()
                                    .jwt(jwt -> jwt.claim("sub", subject)))
            );

            // THEN
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(UserConstants.STATUS_200))
                    .andExpect(jsonPath("$.statusMsg").value(UserConstants.MESSAGE_200));
            verify(userService, times(1)).deleteUserAndTransactions(subject);
        }

        @Test
        void shouldThrowResourceNotFoundException() throws Exception {
            // GIVEN
            String subject = "unknown-subject";
            doThrow(new ResourceNotFoundException("User", "keycloakSub", subject))
                    .when(userService)
                    .deleteUserAndTransactions(subject);

            // WHEN
            var resultActions = mockMvc.perform(delete("/api/users")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", subject))));

            // THEN
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString(
                            "User not found with the given input data keycloakSub : 'unknown-subject'"
                    )));
        }
    }

    @Test
    @WithAnonymousUser
    void shouldReturnUnauthorizedIfNoJwt() throws Exception {
        // WHEN
        var resultActions = mockMvc.perform(get("/api/users"));

        // THEN
        resultActions.andExpect(status().isUnauthorized());
    }
}
