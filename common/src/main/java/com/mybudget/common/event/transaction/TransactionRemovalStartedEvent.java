package com.mybudget.common.event.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRemovalStartedEvent {
    private Long transactionId;
    private String keycloakSub;
    private BigDecimal amount;
    private String transactionType;
}
