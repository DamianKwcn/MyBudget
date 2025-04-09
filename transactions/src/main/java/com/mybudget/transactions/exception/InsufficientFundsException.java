package com.mybudget.transactions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String keycloakSub) {
        super(String.format("Insufficient funds for user with keycloakSub: %s", keycloakSub));
    }
}
