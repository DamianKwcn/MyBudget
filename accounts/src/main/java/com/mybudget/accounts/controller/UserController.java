package com.mybudget.accounts.controller;

import com.mybudget.accounts.constants.UserConstants;
import com.mybudget.accounts.dto.BalanceDto;
import com.mybudget.accounts.dto.ResponseDto;
import com.mybudget.accounts.dto.UserDto;
import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.mapper.UserMapper;
import com.mybudget.accounts.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final UserService userService;

    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("java 17");
    }

    @RateLimiter(name = "getUserProfile")
    @GetMapping("/users")
    public ResponseEntity<UserDto> getUserProfile(JwtAuthenticationToken jwtAuthToken ) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();

        User user = userService.findUserByKeycloakSub(keycloakSub);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto userDto = UserMapper.mapToUserDto(user, new UserDto());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDto);
    }

    @RateLimiter(name = "setUserBalance")
    @PostMapping("/users/balance")
    public ResponseEntity<ResponseDto> setUserBalance(@Valid @RequestBody BalanceDto balanceDto) {
        userService.setBalance(balanceDto.getBalance());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(UserConstants.STATUS_200, UserConstants.MESSAGE_200));
    }

}
