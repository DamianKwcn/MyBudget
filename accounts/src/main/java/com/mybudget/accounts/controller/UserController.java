package com.mybudget.accounts.controller;

import com.mybudget.accounts.constants.UserConstants;
import com.mybudget.accounts.dto.ResponseDto;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.accounts.service.implementation.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class UserController {

    private final UserServiceImpl userService;
    private final UserRepository userRepository;

    @GetMapping("/save")
    public ResponseEntity<User> getMyProfile(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        String email = jwtAuthToken.getToken().getClaimAsString("email");
        String firstName = jwtAuthToken.getToken().getClaimAsString("given_name");
        String lastName = jwtAuthToken.getToken().getClaimAsString("family_name");
        String username = jwtAuthToken.getToken().getClaimAsString("preferred_username");

        User profile = userService.getOrCreateProfile(keycloakSub, email, firstName, lastName, username);

        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteUser(@RequestParam Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(UserConstants.STATUS_200, UserConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(UserConstants.STATUS_417, UserConstants.MESSAGE_417_DELETE));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Optional<User>> getLoggedUserProfile(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();

        // Możesz tu wywołać findByKeycloakSub, jeśli chcesz mieć pewność,
        // że w bazie jest już user, bo CustomOAuth2SuccessHandler go stworzył.
        // Albo dalej użyć getOrCreateProfile, jeśli chcesz go utworzyć, gdyby jednak nie istniał.

        Optional<User> user = userRepository.findByKeycloakSub(keycloakSub);

        return ResponseEntity.ok(user);
    }

}