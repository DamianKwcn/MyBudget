package com.mybudget.transactions.controller.feign;

import com.mybudget.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionFeignController {
    private final TransactionService transactionService;

    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllByKeycloakSub(
            @RequestParam String keycloakSub,
            JwtAuthenticationToken jwtAuthToken) {
        String tokenSub = jwtAuthToken.getToken().getSubject();

        if (!tokenSub.equals(keycloakSub)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        transactionService.deleteAllByKeycloakSub(keycloakSub);
        return ResponseEntity.noContent().build();
    }
}
