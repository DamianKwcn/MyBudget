package com.mybudget.accounts.configuration;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountsSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private Jwt mockJwt() {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("realm_access", Map.of("roles", new String[]{"USER"}))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void shouldAllowAccessToInitializeEndpointWithoutAuthentication() throws Exception {
        // Given
        String requestBody = """
            {
                "keycloakSub": "test-sub",
                "email": "test@example.com",
                "givenName": "Test",
                "familyName": "User",
                "preferredUsername": "testuser"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/user/initialize")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointsWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUsersToAccessProtectedEndpoints() throws Exception {
        // Given
        Jwt jwt = mockJwt();
        when(jwtDecoder.decode("mock-token")).thenReturn(jwt);

        User mockUser = new User();
        mockUser.setKeycloakSub("test-user");
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("mockUser");
        mockUser.setBalance(BigDecimal.valueOf(100.0));

        when(userService.findUserByKeycloakSub("test-user")).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointsForUserWithoutRoles() throws Exception {
        // Given
        Jwt noRoleJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(jwtDecoder.decode("mock-token")).thenReturn(noRoleJwt);
        when(userService.findUserByKeycloakSub("test-user")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(noRoleJwt)))
                .andExpect(status().isForbidden());
    }
}
