package com.mybudget.accounts.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserDto {

    @NotEmpty(message = "Username can not be empty")
    @Size(min = 5, max = 30, message = "The length of the username should be between 5 and 30")
    private String username;

    @NotEmpty(message = "Email address can not be empty")
    @Email(message = "Email address should be a valid value")
    private String email;

    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
    private BigDecimal balance;

}

