package com.mybudget.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionConfirmEvent {
    private Long transactionId;
    private String keycloakSub;
    private BigDecimal balanceAfter;
}
