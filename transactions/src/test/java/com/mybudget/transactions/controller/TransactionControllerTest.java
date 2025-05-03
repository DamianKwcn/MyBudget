package com.mybudget.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.CreateTransactionDto;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String subject = "test-subject";

    private Category expenseCategory;
    private Category incomeCategory;
    private Transaction expenseTransaction;
    private Transaction incomeTransaction;
    private CreateTransactionDto createExpenseDto;

    @BeforeEach
    void setUp() {
        expenseCategory = new Category();
        expenseCategory.setId(3L);
        expenseCategory.setCategoryName("Car");
        expenseCategory.setTransactionType(TransactionType.EXPENSE);

        incomeCategory = new Category();
        incomeCategory.setId(1L);
        incomeCategory.setCategoryName("Salary");
        incomeCategory.setTransactionType(TransactionType.INCOME);

        expenseTransaction = new Transaction();
        expenseTransaction.setId(1L);
        expenseTransaction.setKeycloakSub(subject);
        expenseTransaction.setAmount(BigDecimal.valueOf(50.00));
        expenseTransaction.setTransactionType(TransactionType.EXPENSE);
        expenseTransaction.setCategory(expenseCategory);
        expenseTransaction.setDescription("Car repair");
        expenseTransaction.setBalanceAfter(BigDecimal.valueOf(950.00));

        incomeTransaction = new Transaction();
        incomeTransaction.setId(2L);
        incomeTransaction.setKeycloakSub(subject);
        incomeTransaction.setAmount(BigDecimal.valueOf(100.00));
        incomeTransaction.setTransactionType(TransactionType.INCOME);
        incomeTransaction.setCategory(incomeCategory);
        incomeTransaction.setDescription("Monthly salary");
        incomeTransaction.setBalanceAfter(BigDecimal.valueOf(1050.00));

        createExpenseDto = CreateTransactionDto.builder()
                .amount(BigDecimal.valueOf(50.00))
                .categoryId(3L)
                .description("Car repair")
                .transactionType(TransactionType.EXPENSE)
                .build();
    }

    @Test
    void shouldCreateExpenseTransaction() throws Exception {
        // Given
        String username = "test-user";
        when(transactionService.createTransaction(
                eq(subject),
                eq(username),
                eq(createExpenseDto.getAmount()),
                eq(createExpenseDto.getCategoryId()),
                eq(createExpenseDto.getDescription()),
                eq(createExpenseDto.getTransactionType().name())
        )).thenReturn(expenseTransaction);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .with(jwt().jwt(builder -> builder
                                .claim("sub", subject)
                                .claim("preferred_username", username)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExpenseDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(TransactionConstants.STATUS_201))
                .andExpect(jsonPath("$.statusMsg").value(TransactionConstants.MESSAGE_201));

        verify(transactionService, times(1)).createTransaction(
                subject,
                username,
                createExpenseDto.getAmount(),
                createExpenseDto.getCategoryId(),
                createExpenseDto.getDescription(),
                createExpenseDto.getTransactionType().name()
        );
    }

    @Test
    void shouldReturnExpenses() throws Exception {
        // Given
        when(transactionService.findByTransactionType(subject, TransactionType.EXPENSE))
                .thenReturn(List.of(expenseTransaction));

        // When & Then
        mockMvc.perform(get("/api/expenses")
                        .with(jwt().jwt(builder -> builder.claim("sub", subject))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].balanceAfter").value(950.00))
                .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$[0].description").value("Car repair"))
                .andExpect(jsonPath("$[0].categoryName").value("Car"));
    }

    @Test
    void shouldReturnIncomes() throws Exception {
        // Given
        when(transactionService.findByTransactionType(subject, TransactionType.INCOME))
                .thenReturn(List.of(incomeTransaction));

        // When & Then
        mockMvc.perform(get("/api/incomes")
                        .with(jwt().jwt(builder -> builder.claim("sub", subject))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].balanceAfter").value(1050.00))
                .andExpect(jsonPath("$[0].transactionType").value("INCOME"))
                .andExpect(jsonPath("$[0].description").value("Monthly salary"))
                .andExpect(jsonPath("$[0].categoryName").value("Salary"));
    }

    @Test
    void shouldReturnTransactions() throws Exception {
        // Given
        when(transactionService.findTransactions(subject))
                .thenReturn(List.of(expenseTransaction, incomeTransaction));

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .with(jwt().jwt(builder -> builder.claim("sub", subject))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnTransactionById() throws Exception {
        // Given
        Long transactionId = 1L;
        when(transactionService.findByKeycloakSubAndId(subject, transactionId))
                .thenReturn(Optional.of(expenseTransaction));

        // When & Then
        mockMvc.perform(get("/api/transactions/{id}", transactionId)
                        .with(jwt().jwt(builder -> builder.claim("sub", subject))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.balanceAfter").value(950.00))
                .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$.description").value("Car repair"))
                .andExpect(jsonPath("$.categoryName").value("Car"));
    }
}

