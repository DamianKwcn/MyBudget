package com.mybudget.transactions.mapper;

import com.mybudget.transactions.dto.CategoryDto;
import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static Category mapToCategory(CategoryDto categoryDto, Category category){
        category.setId(categoryDto.getId());
        category.setCategoryName(categoryDto.getCategoryName());
        category.setCategoryBalance(categoryDto.getCategoryBalance());
        category.setTransactionType(categoryDto.getTransactionType());
        category.setTransactions(
                categoryDto.getTransactions().stream()
                        .map(transactionDto -> TransactionMapper.mapToTransaction(transactionDto, new Transaction()))
                        .collect(Collectors.toList())
        );
        return category;
    }

    public static CategoryDto mapToCategoryDto(Category category, CategoryDto categoryDto){
        categoryDto.setId(category.getId());
        categoryDto.setCategoryName(category.getCategoryName());
        categoryDto.setCategoryBalance(category.getCategoryBalance());
        categoryDto.setTransactionType(category.getTransactionType());
        categoryDto.setTransactions(
                category.getTransactions().stream()
                        .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                        .collect(Collectors.toList())
        );
        return categoryDto;
    }

    public static List<CategoryDto> toDtoList(Collection<Category> entities) {
        return entities.stream()
                .map(entity -> CategoryMapper.mapToCategoryDto(entity, new CategoryDto()))
                .toList();
    }

    public static Collection<Category> toEntityList(Collection<CategoryDto> dtoList) {
        return dtoList.stream()
                .map(dto -> CategoryMapper.mapToCategory(dto, new Category()))
                .toList();
    }

}
