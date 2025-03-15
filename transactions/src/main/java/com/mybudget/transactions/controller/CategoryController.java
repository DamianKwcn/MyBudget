package com.mybudget.transactions.controller;

import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.CreateCategoryDto;
import com.mybudget.transactions.dto.ResponseDto;
import com.mybudget.transactions.service.CategoryService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @RateLimiter(name = "createCategory")
    @PostMapping
    public ResponseEntity<ResponseDto> createCategory(
            JwtAuthenticationToken jwtAuthToken,
            @Valid @RequestBody CreateCategoryDto createCategoryDTO) {

        String keycloakSub = jwtAuthToken.getToken().getSubject();

        categoryService.createUserCategory(
                keycloakSub,
                createCategoryDTO.getCategoryName(),
                createCategoryDTO.getTransactionType()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(TransactionConstants.STATUS_201, TransactionConstants.MESSAGE_201));
    }
}
