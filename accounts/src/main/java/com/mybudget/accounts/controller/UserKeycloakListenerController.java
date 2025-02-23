package com.mybudget.accounts.controller;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserKeycloakListenerController {
    private final UserRepository userRepository;
    private final UserService userService;

    @Getter
    public static class UserInitRequest {
        private String keycloakSub;
        private String email;
        private String givenName;
        private String familyName;
        private String preferredUsername;
    }

    @Getter
    public static class DeleteUserRequest {
        private String keycloakSub;
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeUser(@RequestBody UserInitRequest userInitRequest) {
        Optional<User> existing = userRepository.findByKeycloakSub(userInitRequest.getKeycloakSub());
        if (existing.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with Keycloak sub already exists.");
        }
        userService.createUser(
                userInitRequest.getKeycloakSub(),
                userInitRequest.getEmail(),
                userInitRequest.getPreferredUsername()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User created successfully.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();

        userService.deleteUserAndTransactions(keycloakSub);
        return ResponseEntity.noContent().build();
    }

}