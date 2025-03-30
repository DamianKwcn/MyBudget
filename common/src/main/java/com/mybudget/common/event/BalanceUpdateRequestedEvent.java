package com.mybudget.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceUpdateRequestedEvent {
    private String keycloakSub;
    private Long transactionId;
    private BigDecimal amount;
    private String transactionType;
}
