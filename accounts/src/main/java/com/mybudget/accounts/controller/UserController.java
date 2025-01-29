package com.mybudget.accounts.controller;

import com.mybudget.accounts.constants.UserConstants;
import com.mybudget.accounts.dto.BalanceDto;
import com.mybudget.accounts.dto.ResponseDto;
import com.mybudget.accounts.dto.UserDto;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.exception.BalanceAlreadySetException;
import com.mybudget.accounts.exception.ResourceNotFoundException;
import com.mybudget.accounts.mapper.UserMapper;
import com.mybudget.accounts.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        path = "/api",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @RateLimiter(name = "getUserProfile")
    @GetMapping("/users")
    public ResponseEntity<UserDto> getUserProfile(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        logger.info("Fetching profile for user with Keycloak subject: {}", keycloakSub);
        try {
            User user = userService.findUserByKeycloakSub(keycloakSub);
            UserDto userDto = UserMapper.mapToUserDto(user, new UserDto());
            logger.debug("Returning user profile for subject {}: {}", keycloakSub, userDto);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userDto);
        } catch (ResourceNotFoundException exception) {
            logger.warn("User not found for Keycloak subject: {}", keycloakSub, exception);
            throw exception;
        }
    }

    @PostMapping("/users/balance")
    public ResponseEntity<ResponseDto> setUserBalance(
            JwtAuthenticationToken jwtAuthToken,
            @Valid @RequestBody BalanceDto balanceDto) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        logger.info("Attempting to set balance for user with Keycloak subject: {}", keycloakSub);

        try {
            userService.setBalance(keycloakSub, balanceDto.getBalance());
            logger.info("Balance set successfully for user with Keycloak subject: {}, amount: {}", keycloakSub, balanceDto.getBalance());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(UserConstants.STATUS_200, UserConstants.MESSAGE_200));
        } catch (BalanceAlreadySetException exception) {
            logger.warn("Failed to set balance: {}", exception.getMessage());
            throw exception;
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<ResponseDto> deleteUser(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        logger.info("Attempting to delete user with Keycloak subject: {}", keycloakSub);
        try {
            userService.deleteUser(keycloakSub);
            logger.info("Successfully deleted user Keycloak subject: {}", keycloakSub);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(UserConstants.STATUS_200, UserConstants.MESSAGE_200));
        } catch (ResourceNotFoundException exception) {
            logger.warn("Failed to delete user: {}", exception.getMessage());
            throw exception;
        }
    }
}