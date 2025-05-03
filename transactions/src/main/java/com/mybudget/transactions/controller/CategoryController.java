package com.mybudget.transactions.controller;

import com.mybudget.transactions.constants.CategoryConstants;
import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.CategoryDto;
import com.mybudget.transactions.dto.CreateCategoryDto;
import com.mybudget.transactions.dto.ResponseDto;
import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.mapper.CategoryMapper;
import com.mybudget.transactions.mapper.TransactionMapper;
import com.mybudget.transactions.service.CategoryService;
import com.mybudget.transactions.service.TransactionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;

    @RateLimiter(name = "createCategory")
    @PostMapping("/categories")
    public ResponseEntity<ResponseDto> createCategory(
            @Valid @RequestBody CreateCategoryDto dto) {

        categoryService.createUserCategory(
                dto.getCategoryName(),
                dto.getTransactionType());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(CategoryConstants.STATUS_201,
                        CategoryConstants.MESSAGE_201));
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(JwtAuthenticationToken jwtAuthToken,
                                                   @PathVariable Long categoryId) {

        String keycloakSub = jwtAuthToken.getToken().getSubject();
        Category category = categoryService.findUserCategory(keycloakSub,categoryId);
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(category, new CategoryDto());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryDto);
    }

    @GetMapping("/categories/type/{transactionType}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(JwtAuthenticationToken jwtAuthToken,
                                                                 @PathVariable("transactionType") TransactionType transactionType) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        List<Category> categories = categoryService.findCategoriesByType(keycloakSub, transactionType);
        List<CategoryDto> categoriesDto = CategoryMapper.toDtoList(categories);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoriesDto);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getUserCategories(JwtAuthenticationToken jwtAuthToken) {

        String keycloakSub = jwtAuthToken.getToken().getSubject();

        List<CategoryDto> categoryDtos = CategoryMapper.toDtoList(
                categoryService.findUserCategories(keycloakSub));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryDtos);
    }

    @GetMapping("/categories/{categoryId}/transactions")
    public ResponseEntity<List<TransactionDto>> getCategoryTransactions(
            JwtAuthenticationToken jwtAuthToken,
            @PathVariable Long categoryId) {

        String keycloakSub = jwtAuthToken.getToken().getSubject();
        List<Transaction> transactions = transactionService.findByCategory(keycloakSub, categoryId);

        List<TransactionDto> dtos = transactions.stream()
                .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseDto> deleteCategory(JwtAuthenticationToken jwtAuthToken,
                                                      @PathVariable Long categoryId) {

        String keycloakSub = jwtAuthToken.getToken().getSubject();

        categoryService.deleteCategory(keycloakSub, categoryId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(
                        CategoryConstants.STATUS_200,
                        CategoryConstants.MESSAGE_200
                ));
    }

}
