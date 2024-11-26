package com.mybudget.accounts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotEmpty(message = "Name can not be empty")
    @Size(min = 5, max = 30, message = "The length of the customer name should be between 5 and 30")
    private String name;

    @NotEmpty(message = "Email address can not be empty")
    @Email(message = "Email address should be a valid value")
    private String email;

    @NotEmpty(message = "Password can not be empty")
    @Size(min = 8, message = "The length of the password can not be shorter than 8 characters")
    private String password;
}
