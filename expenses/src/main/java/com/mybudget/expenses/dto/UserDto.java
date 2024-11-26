package com.mybudget.expenses.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    @NotEmpty(message = "Name can not be empty")
    @Size(min = 5, max = 30, message = "The length of the user name should be between 5 and 30")
    private String name;

    @NotEmpty(message = "Email address can not be empty")
    @Email(message = "Email address should be a valid value")
    private String email;

    @NotEmpty(message = "Mobile Number can not be a null or empty")
    @Pattern(regexp = "(^$|[0-9]{9})", message = "Mobile Number must be 9 digits")
    private String mobileNumber;

    private Long balance;
}
