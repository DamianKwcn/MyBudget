package com.mybudget.transactions.controller;

import com.mybudget.transactions.constants.CategoryConstants;
import com.mybudget.transactions.dto.CategoryDto;
import com.mybudget.transactions.dto.CreateCategoryDto;
import com.mybudget.transactions.dto.ResponseDto;
import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.mapper.CategoryMapper;
import com.mybudget.transactions.mapper.TransactionMapper;
import com.mybudget.transactions.service.CategoryCommandService;
import com.mybudget.transactions.service.CategoryQueryService;
import com.mybudget.transactions.service.TransactionQueryService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        path = "/api",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class CategoryController {
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final TransactionQueryService transactionQueryService;

    @RateLimiter(name = "createCategory")
    @PostMapping("/categories")
    public ResponseEntity<ResponseDto> createCategory(
            @Valid @RequestBody CreateCategoryDto dto) {

        categoryCommandService.createUserCategory(
                dto.getCategoryName(),
                dto.getTransactionType());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(CategoryConstants.STATUS_201,
                        CategoryConstants.MESSAGE_201));
    }

    @RateLimiter(name = "getCategory")
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {

        Category category = categoryQueryService.findUserCategory(categoryId);
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(category, new CategoryDto());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryDto);
    }

    @RateLimiter(name = "getCategoriesByType")
    @GetMapping("/categories/type/{transactionType}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(@PathVariable("transactionType") TransactionType transactionType) {
        List<Category> categories = categoryQueryService.findCategoriesByType(transactionType);
        List<CategoryDto> categoriesDto = CategoryMapper.toDtoList(categories);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoriesDto);
    }

    @RateLimiter(name = "getUserCategories")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getUserCategories() {
        List<CategoryDto> categoryDtos = CategoryMapper.toDtoList(
                categoryQueryService.findUserCategories());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryDtos);
    }

    @RateLimiter(name = "getCategoryTransactions")
    @GetMapping("/categories/{categoryId}/transactions")
    public ResponseEntity<List<TransactionDto>> getCategoryTransactions(@PathVariable Long categoryId) {
        List<Transaction> transactions = transactionQueryService.findByCategory(categoryId);

        List<TransactionDto> dtos = transactions.stream()
                .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @RateLimiter(name = "deleteCategory")
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseDto> deleteCategory(@PathVariable Long categoryId) {
        categoryCommandService.deleteCategory(categoryId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(
                        CategoryConstants.STATUS_200,
                        CategoryConstants.MESSAGE_200
                ));
    }

}
