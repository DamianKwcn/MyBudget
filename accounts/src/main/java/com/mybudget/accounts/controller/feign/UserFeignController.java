package com.mybudget.accounts.controller.feign;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.entity.feign.BalanceUpdateRequest;
import com.mybudget.accounts.entity.feign.TransactionType;
import com.mybudget.accounts.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        path = "/api",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class UserFeignController {
    private final UserService userService;

    @RateLimiter(name = "updateBalance")
    @PutMapping("/update-balance")
    public ResponseEntity<Void> updateBalance(@RequestBody @Valid BalanceUpdateRequest request) {
        userService.updateBalance(request.getKeycloakSub(),
                request.getAmount(),
                request.getTransactionType() == TransactionType.INCOME);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-balance-after-delete")
    public ResponseEntity<Void> updateBalanceAfterDelete(@RequestBody @Valid BalanceUpdateRequest request) {
        userService.updateBalanceAfterDelete(request.getKeycloakSub(),
                request.getAmount(),
                request.getTransactionType() == TransactionType.INCOME);

        return ResponseEntity.ok().build();
    }

    @RateLimiter(name = "getUserBalance")
    @GetMapping("/users/balance/{keycloakSub}")
    public ResponseEntity<BigDecimal> getUserBalance(@PathVariable String keycloakSub) {
        User user = userService.findUserByKeycloakSub(keycloakSub);
        return ResponseEntity.ok(user.getBalance());
    }
}
