package com.mybudget.accounts.configuration;

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
        mockMvc.perform(post("/api/user/initialize"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUsersToAccessProtectedEndpoints() throws Exception {
        when(jwtDecoder.decode("mock-token")).thenReturn(mockJwt());

        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(mockJwt())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointsForUserWithoutRoles() throws Exception {
        Jwt noRoleJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(jwtDecoder.decode("mock-token")).thenReturn(noRoleJwt);

        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(noRoleJwt)))
                .andExpect(status().isForbidden());
    }
}