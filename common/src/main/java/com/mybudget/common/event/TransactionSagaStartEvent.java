package com.mybudget.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSagaStartEvent {
    private Long transactionId;
    private String keycloakSub;
    private BigDecimal amount;
    private String transactionType;
}

