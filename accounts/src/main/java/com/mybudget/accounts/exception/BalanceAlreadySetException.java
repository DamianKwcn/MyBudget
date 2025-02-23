package com.mybudget.accounts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class BalanceAlreadySetException extends RuntimeException {
    public BalanceAlreadySetException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s already set for user with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
