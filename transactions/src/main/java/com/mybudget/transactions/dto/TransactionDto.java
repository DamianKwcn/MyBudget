package com.mybudget.transactions.dto;

import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

import java.math.BigDecimal;

@Data
public class TransactionDto {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a numeric value with up to 2 decimal places")
    private BigDecimal amount;

    private BigDecimal balanceAfter;

    @NotNull(message = "Type cannot be null")
    private TransactionType transactionType = TransactionType.EXPENSE;

    @Size(max = 255, message = "Description can have up to 255 characters")
    private String description;

    private LocalDateTime createdAt;

}
