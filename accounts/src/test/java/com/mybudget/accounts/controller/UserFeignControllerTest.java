package com.mybudget.accounts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.entity.feign.BalanceUpdateRequest;
import com.mybudget.accounts.entity.feign.TransactionType;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserFeignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("PUT /api/update-balance - updateBalance()")
    class UpdateBalanceTests {

        @Test
        void shouldUpdateBalanceForIncome() throws Exception {
            // GIVEN
            BalanceUpdateRequest request = new BalanceUpdateRequest("test-subject", BigDecimal.valueOf(500.00), TransactionType.INCOME);
            doNothing().when(userService).updateBalance(eq(request.getKeycloakSub()), eq(request.getAmount()), eq(true));

            // WHEN
            var resultActions = mockMvc.perform(put("/api/update-balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", request.getKeycloakSub())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // THEN
            resultActions
                    .andExpect(status().isOk());

            verify(userService, times(1)).updateBalance(request.getKeycloakSub(), request.getAmount(), true);
        }

        @Test
        void shouldUpdateBalanceForExpense() throws Exception {
            // GIVEN
            BalanceUpdateRequest request = new BalanceUpdateRequest("test-subject", BigDecimal.valueOf(200.00), TransactionType.EXPENSE);
            doNothing().when(userService).updateBalance(eq(request.getKeycloakSub()), eq(request.getAmount()), eq(false));

            // WHEN
            var resultActions = mockMvc.perform(put("/api/update-balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", request.getKeycloakSub())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // THEN
            resultActions
                    .andExpect(status().isOk());

            verify(userService, times(1)).updateBalance(request.getKeycloakSub(), request.getAmount(), false);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() throws Exception {
            // GIVEN
            BalanceUpdateRequest request = new BalanceUpdateRequest("unknown-subject", BigDecimal.valueOf(100.00), TransactionType.INCOME);
            doThrow(new ResourceNotFoundException("User", "keycloakSub", request.getKeycloakSub()))
                    .when(userService).updateBalance(eq(request.getKeycloakSub()), eq(request.getAmount()), eq(true));

            // WHEN
            var resultActions = mockMvc.perform(put("/api/update-balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", request.getKeycloakSub())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // THEN
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("User not found with the given input data keycloakSub")));

            verify(userService, times(1)).updateBalance(request.getKeycloakSub(), request.getAmount(), true);
        }

        @Test
        void shouldReturnBadRequestIfMissingFields() throws Exception {
            // GIVEN
            BalanceUpdateRequest request = new BalanceUpdateRequest(null, null, null);

            // WHEN
            var resultActions = mockMvc.perform(put("/api/update-balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // THEN
            resultActions
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{keycloakSub}/balance - getUserBalance()")
    class GetUserBalanceTests {

        @Test
        void shouldReturnUserBalance() throws Exception {
            // GIVEN
            String keycloakSub = "test-subject";
            User user = new User();
            user.setKeycloakSub(keycloakSub);
            user.setBalance(BigDecimal.valueOf(1200.50));

            BDDMockito.given(userService.findUserByKeycloakSub(keycloakSub)).willReturn(user);

            // WHEN
            var resultActions = mockMvc.perform(get("/api/users/balance/{keycloakSub}", keycloakSub)
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", keycloakSub))));

            // THEN
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(1200.50));

            verify(userService, times(1)).findUserByKeycloakSub(keycloakSub);
        }

        @Test
        void shouldReturnNotFoundIfUserDoesNotExist() throws Exception {
            // GIVEN
            String keycloakSub = "unknown-subject";
            BDDMockito.given(userService.findUserByKeycloakSub(keycloakSub))
                    .willThrow(new ResourceNotFoundException("User", "keycloakSub", keycloakSub));

            // WHEN
            var resultActions = mockMvc.perform(get("/api/users/balance/{keycloakSub}", keycloakSub)
                    .with(SecurityMockMvcRequestPostProcessors.jwt()
                            .jwt(jwt -> jwt.claim("sub", keycloakSub))));

            // THEN
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("User not found with the given input data keycloakSub")));

            verify(userService, times(1)).findUserByKeycloakSub(keycloakSub);
        }
    }
}

