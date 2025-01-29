package com.mybudget.accounts.controller;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserRegistrationController {
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
}
