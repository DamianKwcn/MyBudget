package com.mybudget.accounts.common;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {
    private JwtAuthenticationToken jwt() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }

    @Override public String getKeycloakSub() {
        return jwt().getToken().getSubject();
    }

    @Override public String getUsername() {
        return jwt().getToken().getClaimAsString("preferred_username");
    }
}
