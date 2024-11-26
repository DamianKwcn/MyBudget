package com.mybudget.expenses.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ExpenseAlreadyExistsException extends RuntimeException{
    public ExpenseAlreadyExistsException(String message){
        super(message);
    }
}
