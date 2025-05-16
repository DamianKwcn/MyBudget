package com.mybudget.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.CreateTransactionDto;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.service.TransactionCommandService;
import com.mybudget.transactions.service.TransactionQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private TransactionCommandService transactionCommandService;

    @MockBean
    private TransactionQueryService transactionQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String sub = "test-subject";

    private Transaction expense;
    private Transaction income;

    @BeforeEach
    void setUp() {
        expense = new Transaction();
        expense.setId(1L);
        expense.setKeycloakSub(sub);
        expense.setAmount(BigDecimal.valueOf(50));
        expense.setBalanceAfter(BigDecimal.valueOf(950));
        expense.setTransactionType(TransactionType.EXPENSE);
        Category cat1 = new Category();
        cat1.setCategoryName("Car");
        expense.setCategory(cat1);
        expense.setDescription("Car repair");
        expense.setCreatedAt(LocalDateTime.of(2025,5,15,12,0));

        income = new Transaction();
        income.setId(2L);
        income.setKeycloakSub(sub);
        income.setAmount(BigDecimal.valueOf(100));
        income.setBalanceAfter(BigDecimal.valueOf(1050));
        income.setTransactionType(TransactionType.INCOME);
        Category cat2 = new Category();
        cat2.setCategoryName("Salary");
        income.setCategory(cat2);
        income.setDescription("Monthly salary");
        income.setCreatedAt(LocalDateTime.of(2025,5,14,9,30));
    }

    @Nested
    @DisplayName("POST /api/transactions")
    class CreateTransactionTests {
        @Test
        void shouldCreateTransactionAndReturn201() throws Exception {
            // GIVEN
            CreateTransactionDto dto = CreateTransactionDto.builder()
                    .amount(BigDecimal.valueOf(50))
                    .categoryId(3L)
                    .description("Test desc")
                    .build();

            // WHEN
            var result = mockMvc.perform(post("/api/transactions")
                    .with(jwt().jwt(b -> b.claim("sub", sub)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(TransactionConstants.STATUS_201))
                    .andExpect(jsonPath("$.statusMsg").value(TransactionConstants.MESSAGE_201));

            verify(transactionCommandService, times(1))
                    .createTransaction(eq(dto.getAmount()), eq(dto.getCategoryId()), eq(dto.getDescription()));
        }
    }

    @Nested
    @DisplayName("GET /api/transactions")
    class GetTransactionsTests {
        @Test
        void shouldReturnAllTransactions() throws Exception {
            // GIVEN
            when(transactionQueryService.findTransactions())
                    .thenReturn(List.of(expense, income));

            // WHEN
            var result = mockMvc.perform(get("/api/transactions")
                    .with(jwt().jwt(b -> b.claim("sub", sub))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].amount").value(50.0))
                    .andExpect(jsonPath("$[0].balanceAfter").value(950.0))
                    .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"))
                    .andExpect(jsonPath("$[0].description").value("Car repair"))
                    .andExpect(jsonPath("$[0].categoryName").value("Car"));

            verify(transactionQueryService, times(1)).findTransactions();
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/{id}")
    class GetTransactionByIdTests {
        @Test
        void shouldReturnTransactionDto() throws Exception {
            // GIVEN
            when(transactionQueryService.findByKeycloakSubAndId(1L))
                    .thenReturn(expense);

            // WHEN
            var result = mockMvc.perform(get("/api/transactions/1")
                    .with(jwt().jwt(b -> b.claim("sub", sub))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.amount").value(50.0))
                    .andExpect(jsonPath("$.balanceAfter").value(950.0))
                    .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
                    .andExpect(jsonPath("$.description").value("Car repair"))
                    .andExpect(jsonPath("$.categoryName").value("Car"));

            verify(transactionQueryService, times(1)).findByKeycloakSubAndId(1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/transactions/{id}")
    class DeleteTransactionTests {
        @Test
        void shouldDeleteTransactionAndReturn200() throws Exception {
            // WHEN
            var result = mockMvc.perform(delete("/api/transactions/1")
                    .with(jwt().jwt(b -> b.claim("sub", sub))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(TransactionConstants.STATUS_200))
                    .andExpect(jsonPath("$.statusMsg").value(TransactionConstants.MESSAGE_200));

            verify(transactionCommandService, times(1)).deleteTransaction(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/expenses")
    class GetExpensesTests {
        @Test
        void shouldReturnExpensesList() throws Exception {
            // GIVEN
            when(transactionQueryService.findByTransactionType(TransactionType.EXPENSE))
                    .thenReturn(List.of(expense));

            // WHEN
            var result = mockMvc.perform(get("/api/expenses")
                    .with(jwt().jwt(b -> b.claim("sub", sub))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].amount").value(50.0))
                    .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"));

            verify(transactionQueryService, times(1))
                    .findByTransactionType(TransactionType.EXPENSE);
        }
    }

    @Nested
    @DisplayName("GET /api/incomes")
    class GetIncomesTests {
        @Test
        void shouldReturnIncomesList() throws Exception {
            // GIVEN
            when(transactionQueryService.findByTransactionType(TransactionType.INCOME))
                    .thenReturn(List.of(income));

            // WHEN
            var result = mockMvc.perform(get("/api/incomes")
                    .with(jwt().jwt(b -> b.claim("sub", sub))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].amount").value(100.0))
                    .andExpect(jsonPath("$[0].transactionType").value("INCOME"));

            verify(transactionQueryService, times(1))
                    .findByTransactionType(TransactionType.INCOME);
        }
    }
}
