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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
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
            String sub = "test-subject";
            User user = new User();
            user.setId(1L);
            user.setKeycloakSub(sub);
            user.setUsername("test-user");
            user.setEmail("test@example.com");
            user.setBalance(BigDecimal.TEN);
            BDDMockito.given(userService.findUserByKeycloakSub(sub)).willReturn(user);

            // WHEN
            var result = mockMvc.perform(get("/api/users")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.claim("sub", sub))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("test-user"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.balance").value(10));
            verify(userService, times(1)).findUserByKeycloakSub(sub);
        }

        @Test
        void shouldThrowResourceNotFoundException() throws Exception {
            // GIVEN
            String sub = "unknown-subject";
            BDDMockito.given(userService.findUserByKeycloakSub(sub))
                    .willThrow(new ResourceNotFoundException("User", "keycloakSub", sub));

            // WHEN
            var result = mockMvc.perform(get("/api/users")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.claim("sub", sub))));

            // THEN
            result.andExpect(status().isNotFound())
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
            BigDecimal newBal = BigDecimal.valueOf(1000.00);
            BalanceDto dto = new BalanceDto();
            dto.setBalance(newBal);
            doNothing().when(userService).setBalance(eq(newBal));

            // WHEN
            var result = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.claim("sub", "irrelevant")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(UserConstants.STATUS_200))
                    .andExpect(jsonPath("$.statusMsg").value(UserConstants.MESSAGE_200));
            verify(userService, times(1)).setBalance(newBal);
        }

        @Test
        void shouldThrowBalanceAlreadySetException() throws Exception {
            // GIVEN
            BigDecimal newBal = BigDecimal.valueOf(1000.00);
            BalanceDto dto = new BalanceDto();
            dto.setBalance(newBal);
            doThrow(new BalanceAlreadySetException("Balance", "keycloakSub", "irrelevant"))
                    .when(userService).setBalance(eq(newBal));

            // WHEN
            var result = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.claim("sub", "irrelevant")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isNotAcceptable())
                    .andExpect(content().string(containsString("Balance already set for user")));
        }

        @Test
        void shouldValidateNegativeBalance() throws Exception {
            // GIVEN
            BalanceDto dto = new BalanceDto();
            dto.setBalance(BigDecimal.valueOf(-1));

            // WHEN
            var result = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.claim("sub", "irrelevant")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value(containsString("Balance must be non-negative")));
        }

        @Test
        void shouldValidateMissingBalanceField() throws Exception {
            // GIVEN
            String body = "{}";

            // WHEN
            var result = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(t -> t.claim("sub","x")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // THEN
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage")
                            .value(containsString("Balance cannot be null")));
        }

        @Test
        void shouldAcceptZeroBalance() throws Exception {
            // GIVEN
            BalanceDto dto = new BalanceDto();
            dto.setBalance(BigDecimal.ZERO);
            doNothing().when(userService).setBalance(eq(BigDecimal.ZERO));

            // WHEN
            var result = mockMvc.perform(post("/api/users/balance")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(t -> t.claim("sub","x")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(UserConstants.STATUS_200))
                    .andExpect(jsonPath("$.statusMsg").value(UserConstants.MESSAGE_200));
            verify(userService).setBalance(BigDecimal.ZERO);
        }
    }

    @Test
    @WithAnonymousUser
    void shouldReturnUnauthorizedIfNoJwt() throws Exception {
        // WHEN
        var result = mockMvc.perform(get("/api/users"));
        // THEN
        result.andExpect(status().isUnauthorized());
    }
}
