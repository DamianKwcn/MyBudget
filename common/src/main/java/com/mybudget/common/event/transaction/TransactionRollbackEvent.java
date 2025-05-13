package com.mybudget.common.event.transaction;

import com.mybudget.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRollbackEvent {
    private String keycloakSub;
    private Long transactionId;
    private TransactionStatus transactionStatus;
}