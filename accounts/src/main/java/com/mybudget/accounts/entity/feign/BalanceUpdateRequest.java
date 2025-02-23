package com.mybudget.accounts.entity.feign;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceUpdateRequest {
    @NotNull(message = "KeycloakSub cannot be null")
    private String keycloakSub;

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType transactionType;
}

