package com.mybudget.transactions.dto;

import com.mybudget.transactions.entity.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDto {

    @NotNull(message = "Category name cannot be null")
    @Size(max = 20, message = "Category name can have up to 20 characters")
    private String categoryName;

    @NotNull(message = "Category type cannot be null")
    private TransactionType transactionType;

}
