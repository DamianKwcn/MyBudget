package com.mybudget.common.event.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.common.enums.TransactionType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreatedEvent {
    private String keycloakSub;
    private Long transactionId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
}