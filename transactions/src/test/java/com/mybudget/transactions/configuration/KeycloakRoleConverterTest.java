package com.mybudget.transactions.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test
    void shouldConvertRealmRolesCorrectly() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of(
                "realm_access", Map.of("roles", List.of("admin", "user"))
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_admin", "ROLE_user");
    }

    @Test
    void shouldConvertClientRolesCorrectly() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of(
                "resource_access", Map.of("my-budget-ac", Map.of("roles", List.of("manager", "user")))
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_manager", "ROLE_user");
    }

    @Test
    void shouldConvertRealmAndClientRolesTogether() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of(
                "realm_access", Map.of("roles", List.of("admin")),
                "resource_access", Map.of("my-budget-ac", Map.of("roles", List.of("user")))
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).hasSize(2);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_admin", "ROLE_user");
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoRoles() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", Map.of()));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoRealmAccess() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of());

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isEmpty();
    }
}
