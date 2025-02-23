package com.mybudget.accounts.controller;

import com.mybudget.accounts.constants.UserConstants;
import com.mybudget.accounts.dto.BalanceDto;
import com.mybudget.accounts.dto.ResponseDto;
import com.mybudget.accounts.dto.UserDto;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.mapper.UserMapper;
import com.mybudget.accounts.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;

    @RateLimiter(name = "getUserProfile")
    @GetMapping("/users")
    public ResponseEntity<UserDto> getUserProfile(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();

        User user = userService.findUserByKeycloakSub(keycloakSub);
        UserDto userDto = UserMapper.mapToUserDto(user, new UserDto());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDto);
    }

    @PostMapping("/users/balance")
    public ResponseEntity<ResponseDto> setUserBalance(JwtAuthenticationToken jwtAuthToken,
                                                      @Valid @RequestBody BalanceDto balanceDto) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();

        userService.setBalance(keycloakSub, balanceDto.getBalance());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(UserConstants.STATUS_200, UserConstants.MESSAGE_200));
    }

}
