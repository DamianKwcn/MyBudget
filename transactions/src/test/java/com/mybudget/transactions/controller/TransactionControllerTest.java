package com.mybudget.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.transactions.dto.ExpenseDto;
import com.mybudget.transactions.dto.IncomeDto;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.ExpenseCategory;
import com.mybudget.transactions.entity.enums.IncomeCategory;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.service.TransactionService;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Nested
    @DisplayName("POST /api/expense - createExpense()")
    class CreateExpenseTests {
        @Test
        void shouldCreateExpense() throws Exception {
            ExpenseDto expenseDtoRequest = new ExpenseDto();
            expenseDtoRequest.setAmount(BigDecimal.valueOf(50.00));
            expenseDtoRequest.setTransactionType(TransactionType.EXPENSE);
            expenseDtoRequest.setExpenseCategory(ExpenseCategory.HOME);
            expenseDtoRequest.setDescription("Lunch");

            Transaction transaction = new Transaction();
            transaction.setId(1L);
            transaction.setKeycloakSub(subject);
            transaction.setAmount(expenseDtoRequest.getAmount());
            transaction.setTransactionType(expenseDtoRequest.getTransactionType());
            transaction.setExpenseCategory(expenseDtoRequest.getExpenseCategory());
            transaction.setDescription(expenseDtoRequest.getDescription());
            transaction.setBalanceAfter(BigDecimal.valueOf(950.00));

            when(transactionService.createExpense(
                    eq(subject),
                    eq(expenseDtoRequest.getAmount()),
                    eq(expenseDtoRequest.getTransactionType()),
                    eq(expenseDtoRequest.getExpenseCategory()),
                    eq(expenseDtoRequest.getDescription())))
                    .thenReturn(transaction);

            // WHEN
            var resultActions = mockMvc.perform(post("/api/expense")
                    .with(jwt().jwt(builder -> builder.claim("sub", subject)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expenseDtoRequest)));

            // THEN
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.amount").value(50.0))
                    .andExpect(jsonPath("$.transactionType").value("OUTCOME"))
                    .andExpect(jsonPath("$.expenseCategory").value("HOME"))
                    .andExpect(jsonPath("$.description").value("Lunch"));

            verify(transactionService, times(1))
                    .createExpense(subject,
                            expenseDtoRequest.getAmount(),
                            expenseDtoRequest.getTransactionType(),
                            expenseDtoRequest.getExpenseCategory(),
                            expenseDtoRequest.getDescription());
        }
    }

    @Nested
    @DisplayName("POST /api/income - createIncome()")
    class CreateIncomeTests {
        @Test
        void shouldCreateIncome() throws Exception {
            // GIVEN
            IncomeDto incomeDtoRequest = new IncomeDto();
            incomeDtoRequest.setAmount(BigDecimal.valueOf(100.00));
            incomeDtoRequest.setTransactionType(TransactionType.INCOME);
            incomeDtoRequest.setIncomeCategory(IncomeCategory.SALARY);
            incomeDtoRequest.setDescription("Monthly salary");

            Transaction transaction = new Transaction();
            transaction.setId(2L);
            transaction.setKeycloakSub(subject);
            transaction.setAmount(incomeDtoRequest.getAmount());
            transaction.setTransactionType(incomeDtoRequest.getTransactionType());
            transaction.setIncomeCategory(incomeDtoRequest.getIncomeCategory());
            transaction.setDescription(incomeDtoRequest.getDescription());
            transaction.setBalanceAfter(BigDecimal.valueOf(1050.00));

            when(transactionService.createIncome(
                    eq(subject),
                    eq(incomeDtoRequest.getAmount()),
                    eq(incomeDtoRequest.getTransactionType()),
                    eq(incomeDtoRequest.getIncomeCategory()),
                    eq(incomeDtoRequest.getDescription())))
                    .thenReturn(transaction);

            // WHEN
            var resultActions = mockMvc.perform(post("/api/income")
                    .with(jwt().jwt(builder -> builder.claim("sub", subject)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incomeDtoRequest)));

            // THEN
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.transactionType").value("INCOME"))
                    .andExpect(jsonPath("$.incomeCategory").value("SALARY"))
                    .andExpect(jsonPath("$.description").value("Monthly salary"));

            verify(transactionService, times(1))
                    .createIncome(subject,
                            incomeDtoRequest.getAmount(),
                            incomeDtoRequest.getTransactionType(),
                            incomeDtoRequest.getIncomeCategory(),
                            incomeDtoRequest.getDescription());
        }
    }

    @Nested
    @DisplayName("GET /api/expenses - getExpenses()")
    class GetExpensesTests {
        @Test
        void shouldReturnExpenses() throws Exception {
            // GIVEN
            Transaction transaction = new Transaction();
            transaction.setId(1L);
            transaction.setKeycloakSub(subject);
            transaction.setAmount(BigDecimal.valueOf(50.00));
            transaction.setTransactionType(TransactionType.EXPENSE);
            transaction.setExpenseCategory(ExpenseCategory.HOME);
            transaction.setDescription("Lunch");
            transaction.setBalanceAfter(BigDecimal.valueOf(950.00));

            when(transactionService.findByTransactionType(subject, TransactionType.EXPENSE))
                    .thenReturn(List.of(transaction));

            // WHEN
            var resultActions = mockMvc.perform(get("/api/expenses")
                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));

            // THEN
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].amount").value(50.00))
                    .andExpect(jsonPath("$[0].transactionType").value("OUTCOME"))
                    .andExpect(jsonPath("$[0].expenseCategory").value("HOME"))
                    .andExpect(jsonPath("$[0].description").value("Lunch"));

            verify(transactionService, times(1))
                    .findByTransactionType(subject, TransactionType.EXPENSE);
        }
    }

    @Nested
    @DisplayName("GET /api/incomes - getIncomes()")
    class GetIncomesTests {
        @Test
        void shouldReturnIncomes() throws Exception {
            // GIVEN
            Transaction transaction = new Transaction();
            transaction.setId(2L);
            transaction.setKeycloakSub(subject);
            transaction.setAmount(BigDecimal.valueOf(100.00));
            transaction.setTransactionType(TransactionType.INCOME);
            transaction.setIncomeCategory(IncomeCategory.SALARY);
            transaction.setDescription("Monthly salary");
            transaction.setBalanceAfter(BigDecimal.valueOf(1050.00));

            when(transactionService.findByTransactionType(subject, TransactionType.INCOME))
                    .thenReturn(List.of(transaction));

            // WHEN
            var resultActions = mockMvc.perform(get("/api/incomes")
                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));

            // THEN
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].amount").value(100.00))
                    .andExpect(jsonPath("$[0].transactionType").value("INCOME"))
                    .andExpect(jsonPath("$[0].incomeCategory").value("SALARY"))
                    .andExpect(jsonPath("$[0].description").value("Monthly salary"));

            verify(transactionService, times(1))
                    .findByTransactionType(subject, TransactionType.INCOME);
        }
    }

    @Nested
    @DisplayName("GET /api/transactions - getTransactions()")
    class GetTransactionsTests {
        @Test
        void shouldReturnTransactions() throws Exception {
            // GIVEN
            Transaction transaction1 = new Transaction();
            transaction1.setId(1L);
            transaction1.setKeycloakSub(subject);
            transaction1.setAmount(BigDecimal.valueOf(50.00));
            transaction1.setTransactionType(TransactionType.EXPENSE);
            transaction1.setExpenseCategory(ExpenseCategory.HOME);
            transaction1.setDescription("Lunch");
            transaction1.setBalanceAfter(BigDecimal.valueOf(950.00));

            Transaction transaction2 = new Transaction();
            transaction2.setId(2L);
            transaction2.setKeycloakSub(subject);
            transaction2.setAmount(BigDecimal.valueOf(100.00));
            transaction2.setTransactionType(TransactionType.INCOME);
            transaction2.setIncomeCategory(IncomeCategory.SALARY);
            transaction2.setDescription("Monthly salary");
            transaction2.setBalanceAfter(BigDecimal.valueOf(1050.00));

            when(transactionService.findTransactions(subject))
                    .thenReturn(List.of(transaction1, transaction2));

            // WHEN
            var resultActions = mockMvc.perform(get("/api/transactions")
                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));

            // THEN
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].amount").value(50.00))
                    .andExpect(jsonPath("$[0].balanceAfter").value(950.00))
                    .andExpect(jsonPath("$[0].transactionType").value("OUTCOME"))
                    .andExpect(jsonPath("$[0].description").value("Lunch"))

                    .andExpect(jsonPath("$[1].amount").value(100.00))
                    .andExpect(jsonPath("$[1].balanceAfter").value(1050.00))
                    .andExpect(jsonPath("$[1].transactionType").value("INCOME"))
                    .andExpect(jsonPath("$[1].description").value("Monthly salary"));

            verify(transactionService, times(1)).findTransactions(subject);
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/{id} - getTransactionById()")
    class GetTransactionByIdTests {
        @Test
        void shouldReturnTransactionById() throws Exception {
            // GIVEN
            Long transactionId = 1L;
            Transaction transaction = new Transaction();
            transaction.setId(transactionId);
            transaction.setKeycloakSub(subject);
            transaction.setAmount(BigDecimal.valueOf(50.00));
            transaction.setTransactionType(TransactionType.EXPENSE);
            transaction.setExpenseCategory(ExpenseCategory.HOME);
            transaction.setDescription("Lunch");
            transaction.setBalanceAfter(BigDecimal.valueOf(950.00));

            when(transactionService.findTransaction(subject, transactionId))
                    .thenReturn(Optional.of(transaction));

            // WHEN
            var resultActions = mockMvc.perform(get("/api/transactions/{id}", transactionId)
                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));

            // THEN
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.amount").value(50.00))
                    .andExpect(jsonPath("$.balanceAfter").value(950.00))
                    .andExpect(jsonPath("$.transactionType").value("OUTCOME"))
                    .andExpect(jsonPath("$.description").value("Lunch"));

            verify(transactionService, times(1)).findTransaction(subject, transactionId);
        }
    }
}
