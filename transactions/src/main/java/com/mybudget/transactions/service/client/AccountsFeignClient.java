package com.mybudget.transactions.service.client;

import com.mybudget.transactions.entity.feign.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "accounts", url = "http://localhost:8072/mybudget/accounts/api")
public interface AccountsFeignClient {

    @PutMapping("/update-balance")
    void updateBalance(@RequestHeader("Authorization") String authorizationHeader,
                       @RequestBody BalanceUpdateRequest request);

    @PutMapping("/update-balance-after-delete")
    void updateBalanceAfterDelete(@RequestHeader("Authorization") String authorizationHeader,
                                  @RequestBody BalanceUpdateRequest request);

    @GetMapping("/users/balance/{keycloakSub}")
    BigDecimal getUserBalance(@RequestHeader("Authorization") String authorizationHeader,
                              @PathVariable String keycloakSub);
}

