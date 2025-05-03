package com.mybudget.transactions.dto;

import com.mybudget.transactions.entity.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDto {

    private Long id;
    private String categoryName;
    private TransactionType transactionType;
    private BigDecimal categoryBalance;
    private List<TransactionDto> transactions = new ArrayList<>();

}
