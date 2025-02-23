package com.mybudget.transactions.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test
    void shouldConvertRolesCorrectly() {
        // GIVEN
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of(
                "realm_access", Map.of("roles", List.of("admin", "user"))
        ));

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // THEN
        assertThat(authorities).hasSize(2);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_admin", "ROLE_user");
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoRoles() {
        // GIVEN
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", Map.of()));

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // THEN
        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoRealmAccess() {
        // GIVEN
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of());

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // THEN
        assertThat(authorities).isEmpty();
    }
}
