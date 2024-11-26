package com.mybudget.expenses.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseDto {
    @NotEmpty(message = "Mobile Number can not be a null or empty")
    @Pattern(regexp="(^$|[0-9]{9})",message = "Mobile Number must be 9 digits")
    private String mobileNumber;

    @NotEmpty(message = "Title can not be empty")
    @Size(min = 2, max = 20, message = "The length of the title should be between 2 and 20")
    private String title;

    @NotEmpty(message = "Description can not be empty")
    @Size(max = 100, message = "The length of the description should be between 0 and 100")
    private String description;

    @PositiveOrZero(message = "Total amount used should be equal or greater than zero")
    private BigDecimal amountUsed;
}
