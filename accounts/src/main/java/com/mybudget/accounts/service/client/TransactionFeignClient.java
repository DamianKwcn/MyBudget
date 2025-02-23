package com.mybudget.accounts.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "transaction-service", url = "http://host.docker.internal:8090")
public interface TransactionFeignClient {

    @DeleteMapping("/api/transactions/delete-all")
    void deleteAllTransactions(@RequestParam("keycloakSub") String keycloakSub);
}
