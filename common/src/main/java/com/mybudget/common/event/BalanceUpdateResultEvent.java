package com.mybudget.common.event;

import com.mybudget.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceUpdateResultEvent {
    private String keycloakSub;
    private Long transactionId;
    private BigDecimal balanceAfter;
    private BigDecimal amount;
    private String transactionType;
    private TransactionStatus status;
}