package com.mybudget.common.event.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestedEvent {
    private String keycloakSub;
    private BigDecimal amount;
    private Long categoryId;
    private String description;
}
