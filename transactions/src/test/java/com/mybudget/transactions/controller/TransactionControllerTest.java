//package com.mybudget.transactions.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mybudget.transactions.constants.TransactionConstants;
//import com.mybudget.transactions.dto.CreateTransactionDto;
//import com.mybudget.transactions.entity.Category;
//import com.mybudget.transactions.entity.Transaction;
//import com.mybudget.transactions.entity.enums.TransactionType;
//import com.mybudget.transactions.service.TransactionService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class TransactionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private TransactionService transactionService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private final String subject = "test-subject";
//
//    private Category expenseCategory;
//    private Category incomeCategory;
//    private Transaction expenseTransaction;
//    private Transaction incomeTransaction;
//    private CreateTransactionDto createExpenseDto;
//    private CreateTransactionDto createIncomeDto;
//
//    @BeforeEach
//    void setUp() {
//        // GIVEN
//        expenseCategory = new Category();
//        expenseCategory.setId(3L);
//        expenseCategory.setCategoryName("Car");
//        expenseCategory.setTransactionType(TransactionType.EXPENSE);
//
//        incomeCategory = new Category();
//        incomeCategory.setId(1L);
//        incomeCategory.setCategoryName("Salary");
//        incomeCategory.setTransactionType(TransactionType.INCOME);
//
//        // GIVEN
//        expenseTransaction = new Transaction();
//        expenseTransaction.setId(1L);
//        expenseTransaction.setKeycloakSub(subject);
//        expenseTransaction.setAmount(BigDecimal.valueOf(50.00));
//        expenseTransaction.setTransactionType(TransactionType.EXPENSE);
//        expenseTransaction.setCategory(expenseCategory);
//        expenseTransaction.setDescription("Car repair");
//        expenseTransaction.setBalanceAfter(BigDecimal.valueOf(950.00));
//
//        incomeTransaction = new Transaction();
//        incomeTransaction.setId(2L);
//        incomeTransaction.setKeycloakSub(subject);
//        incomeTransaction.setAmount(BigDecimal.valueOf(100.00));
//        incomeTransaction.setTransactionType(TransactionType.INCOME);
//        incomeTransaction.setCategory(incomeCategory);
//        incomeTransaction.setDescription("Monthly salary");
//        incomeTransaction.setBalanceAfter(BigDecimal.valueOf(1050.00));
//
//        // GIVEN
//        createExpenseDto = new CreateTransactionDto();
//        createExpenseDto.setAmount(BigDecimal.valueOf(50.00));
//        createExpenseDto.setCategoryId(3L);
//        createExpenseDto.setDescription("Car repair");
//
//        createIncomeDto = new CreateTransactionDto();
//        createIncomeDto.setAmount(BigDecimal.valueOf(100.00));
//        createIncomeDto.setCategoryId(1L);
//        createIncomeDto.setDescription("Monthly salary");
//    }
//
//    @Nested
//    @DisplayName("POST /api/transactions - createTransaction()")
//    class CreateTransactionTests {
//        @Test
//        void shouldCreateExpenseTransaction() throws Exception {
//            // GIVEN
//            when(transactionService.createTransaction(
//                    eq(subject),
//                    eq(createExpenseDto.getAmount()),
//                    eq(createExpenseDto.getCategoryId()),
//                    eq(createExpenseDto.getDescription())))
//                    .thenReturn(expenseTransaction);
//            // WHEN
//            var resultActions = mockMvc.perform(post("/api/transactions")
//                    .with(jwt().jwt(builder -> builder.claim("sub", subject)))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(createExpenseDto)));
//            // THEN
//            resultActions.andExpect(status().isCreated())
//                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.statusCode").value(TransactionConstants.STATUS_201))
//                    .andExpect(jsonPath("$.statusMsg").value(TransactionConstants.MESSAGE_201));
//            verify(transactionService, times(1))
//                    .createTransaction(subject,
//                            createExpenseDto.getAmount(),
//                            createExpenseDto.getCategoryId(),
//                            createExpenseDto.getDescription());
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /api/expenses - getExpenses()")
//    class GetExpensesTests {
//        @Test
//        void shouldReturnExpenses() throws Exception {
//            // GIVEN
//            when(transactionService.findByTransactionType(subject, TransactionType.EXPENSE))
//                    .thenReturn(List.of(expenseTransaction));
//            // WHEN
//            var resultActions = mockMvc.perform(get("/api/expenses")
//                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));
//            // THEN
//            resultActions.andExpect(status().isOk())
//                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$", hasSize(1)))
//                    .andExpect(jsonPath("$[0].amount").value(50.00))
//                    .andExpect(jsonPath("$[0].balanceAfter").value(950.00))
//                    .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"))
//                    .andExpect(jsonPath("$[0].description").value("Car repair"))
//                    .andExpect(jsonPath("$[0].categoryName").value("Car"));
//            verify(transactionService, times(1))
//                    .findByTransactionType(subject, TransactionType.EXPENSE);
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /api/incomes - getIncomes()")
//    class GetIncomesTests {
//        @Test
//        void shouldReturnIncomes() throws Exception {
//            // GIVEN
//            when(transactionService.findByTransactionType(subject, TransactionType.INCOME))
//                    .thenReturn(List.of(incomeTransaction));
//            // WHEN
//            var resultActions = mockMvc.perform(get("/api/incomes")
//                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));
//            // THEN
//            resultActions.andExpect(status().isOk())
//                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$", hasSize(1)))
//                    .andExpect(jsonPath("$[0].amount").value(100.00))
//                    .andExpect(jsonPath("$[0].balanceAfter").value(1050.00))
//                    .andExpect(jsonPath("$[0].transactionType").value("INCOME"))
//                    .andExpect(jsonPath("$[0].description").value("Monthly salary"))
//                    .andExpect(jsonPath("$[0].categoryName").value("Salary"));
//            verify(transactionService, times(1))
//                    .findByTransactionType(subject, TransactionType.INCOME);
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /api/transactions - getTransactions()")
//    class GetTransactionsTests {
//        @Test
//        void shouldReturnTransactions() throws Exception {
//            // GIVEN
//            when(transactionService.findTransactions(subject))
//                    .thenReturn(List.of(expenseTransaction, incomeTransaction));
//            // WHEN
//            var resultActions = mockMvc.perform(get("/api/transactions")
//                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));
//            // THEN
//            resultActions.andExpect(status().isOk())
//                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$", hasSize(2)))
//                    .andExpect(jsonPath("$[0].amount").value(50.00))
//                    .andExpect(jsonPath("$[0].balanceAfter").value(950.00))
//                    .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"))
//                    .andExpect(jsonPath("$[0].description").value("Car repair"))
//                    .andExpect(jsonPath("$[0].categoryName").value("Car"))
//                    .andExpect(jsonPath("$[1].amount").value(100.00))
//                    .andExpect(jsonPath("$[1].balanceAfter").value(1050.00))
//                    .andExpect(jsonPath("$[1].transactionType").value("INCOME"))
//                    .andExpect(jsonPath("$[1].description").value("Monthly salary"))
//                    .andExpect(jsonPath("$[1].categoryName").value("Salary"));
//            verify(transactionService, times(1)).findTransactions(subject);
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /api/transactions/{id} - getTransactionById()")
//    class GetTransactionByIdTests {
//        @Test
//        void shouldReturnTransactionById() throws Exception {
//            // GIVEN
//            Long transactionId = 1L;
//            when(transactionService.findTransaction(subject, transactionId))
//                    .thenReturn(Optional.of(expenseTransaction));
//            // WHEN
//            var resultActions = mockMvc.perform(get("/api/transactions/{id}", transactionId)
//                    .with(jwt().jwt(builder -> builder.claim("sub", subject))));
//            // THEN
//            resultActions.andExpect(status().isOk())
//                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.amount").value(50.00))
//                    .andExpect(jsonPath("$.balanceAfter").value(950.00))
//                    .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
//                    .andExpect(jsonPath("$.description").value("Car repair"))
//                    .andExpect(jsonPath("$.categoryName").value("Car"));
//            verify(transactionService, times(1)).findTransaction(subject, transactionId);
//        }
//    }
//}
