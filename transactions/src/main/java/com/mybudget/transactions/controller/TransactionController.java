package com.mybudget.transactions.controller;

import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.CreateTransactionDto;
import com.mybudget.transactions.dto.ResponseDto;
import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.mapper.TransactionMapper;
import com.mybudget.transactions.service.TransactionCommandService;
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
        produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class TransactionController {
    private final TransactionCommandService transactionCommandService;
    private final TransactionQueryService transactionQueryService;

    @RateLimiter(name = "createTransaction")
    @PostMapping("/transactions")
    public ResponseEntity<ResponseDto> createTransaction(@Valid @RequestBody CreateTransactionDto createTransactionDto) {
        transactionCommandService.createTransaction(
                createTransactionDto.getAmount(),
                createTransactionDto.getCategoryId(),
                createTransactionDto.getDescription()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(TransactionConstants.STATUS_201, TransactionConstants.MESSAGE_201));
    }

    @RateLimiter(name = "getTransactions")
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactions() {
        List<Transaction> transactions = transactionQueryService.findTransactions();
        List<TransactionDto> transactionsDto = transactions.stream()
                .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transactionsDto);
    }

    @RateLimiter(name = "getTransaction")
    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable Long id) {
        Transaction transaction = transactionQueryService.findByKeycloakSubAndId(id);
        TransactionDto transactionDto = TransactionMapper.mapToTransactionDto(transaction, new TransactionDto());
        return ResponseEntity.ok(transactionDto);
    }

    @RateLimiter(name = "deleteTransaction")
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<ResponseDto> deleteTransaction(@PathVariable Long id) {
        transactionCommandService.deleteTransaction(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(TransactionConstants.STATUS_200, TransactionConstants.MESSAGE_200));
    }

    @RateLimiter(name = "getExpenses")
    @GetMapping("/expenses")
    public ResponseEntity<List<TransactionDto>> getExpenses() {
        List<Transaction> expenses = transactionQueryService.findByTransactionType(TransactionType.EXPENSE);
        List<TransactionDto> dtos = expenses.stream()
                .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @RateLimiter(name = "getIncomes")
    @GetMapping("/incomes")
    public ResponseEntity<List<TransactionDto>> getIncomes() {
        List<Transaction> incomes = transactionQueryService.findByTransactionType(TransactionType.INCOME);
        List<TransactionDto> dtos = incomes.stream()
                .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

}
