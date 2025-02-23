package com.mybudget.transactions.entity.feign;

import com.mybudget.transactions.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceUpdateRequest {
    private String keycloakSub;
    private BigDecimal amount;
    private TransactionType transactionType;
}
