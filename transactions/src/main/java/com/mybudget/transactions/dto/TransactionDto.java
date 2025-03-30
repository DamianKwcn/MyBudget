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
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private TransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;
    private String categoryName;
}