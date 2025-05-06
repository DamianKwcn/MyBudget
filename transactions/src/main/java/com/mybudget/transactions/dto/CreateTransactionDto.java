package com.mybudget.transactions.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateTransactionDto {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a numeric value with up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "CategoryId cannot be null")
    private Long categoryId;

    @Size(max = 255, message = "Description can have up to 255 characters")
    private String description;

}
