package com.mybudget.transactions.dto;

import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @NotNull(message = "Description cannot be null")
    @Size(max = 255, message = "Description can have up to 255 characters")
    private String description;

    private TransactionStatus transactionStatus;

}
