package com.mybudget.transactions.controller;

import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.ExpenseDto;
import com.mybudget.transactions.dto.IncomeDto;
import com.mybudget.transactions.dto.ResponseDto;
import com.mybudget.transactions.dto.TransactionDto;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.mapper.ExpenseMapper;
import com.mybudget.transactions.mapper.IncomeMapper;
import com.mybudget.transactions.mapper.TransactionMapper;
import com.mybudget.transactions.service.TransactionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        path = "/api",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class TransactionController {
    private final TransactionService transactionService;

    @RateLimiter(name = "createExpense")
    @PostMapping("/expenses")
    public ResponseEntity<ResponseDto> createExpense(JwtAuthenticationToken jwtAuthToken,
                                                    @Valid @RequestBody ExpenseDto expenseDto) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        transactionService.createExpense(keycloakSub,
                                         expenseDto.getAmount(),
                                         expenseDto.getTransactionType(),
                                         expenseDto.getExpenseCategory(),
                                         expenseDto.getDescription());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(TransactionConstants.STATUS_201,TransactionConstants.MESSAGE_201));
    }

    @RateLimiter(name = "createIncome")
    @PostMapping("/incomes")
    public ResponseEntity<ResponseDto> createIncome(JwtAuthenticationToken jwtAuthToken,
                                                    @Valid @RequestBody IncomeDto incomeDto) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        transactionService.createIncome(keycloakSub,
                                        incomeDto.getAmount(),
                                        incomeDto.getTransactionType(),
                                        incomeDto.getIncomeCategory(),
                                        incomeDto.getDescription());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(TransactionConstants.STATUS_201,TransactionConstants.MESSAGE_201));
    }

    @RateLimiter(name = "getExpenses")
    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseDto>> getExpenses(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        List<Transaction> transactions = transactionService.findByTransactionType(keycloakSub, TransactionType.EXPENSE);

        List<ExpenseDto> expenses = transactions.stream()
                .map(transaction -> ExpenseMapper.mapToExpenseDto(transaction, new ExpenseDto()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(expenses);
    }

    @RateLimiter(name = "getIncomes")
    @GetMapping("/incomes")
    public ResponseEntity<List<IncomeDto>> getIncomes(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        List<Transaction> transactions = transactionService.findByTransactionType(keycloakSub, TransactionType.INCOME);

        List<IncomeDto> incomes = transactions.stream()
                .map(transaction -> IncomeMapper.mapToIncomeDto(transaction, new IncomeDto()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(incomes);
    }

    @RateLimiter(name = "getTransactions")
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactions(JwtAuthenticationToken jwtAuthToken) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        List<Transaction> transactions = transactionService.findTransactions(keycloakSub);

        List<TransactionDto> transactionsDto = transactions.stream()
                .map(transaction -> TransactionMapper.mapToTransactionDto(transaction, new TransactionDto()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transactionsDto);
    }

    @RateLimiter(name = "getTransactionById")
    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(JwtAuthenticationToken jwtAuthToken,
                                                             @PathVariable Long id) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        Optional<Transaction> transaction = transactionService.findTransaction(keycloakSub, id);
        TransactionDto transactionDto = TransactionMapper.mapToTransactionDto(transaction.orElseThrow(), new TransactionDto());

        return ResponseEntity.ok(transactionDto);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<ResponseDto> deleteTransaction(JwtAuthenticationToken jwtAuthToken,
                                                         @PathVariable Long id) {
        String keycloakSub = jwtAuthToken.getToken().getSubject();
        transactionService.deleteTransaction(keycloakSub, id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(TransactionConstants.STATUS_200,TransactionConstants.MESSAGE_200));
    }
}
