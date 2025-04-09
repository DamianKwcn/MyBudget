package com.mybudget.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFailedEvent {
    private String keycloakSub;
    private String errorMessage;
}