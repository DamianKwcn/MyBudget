package com.mybudget.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.transactions.constants.CategoryConstants;
import com.mybudget.transactions.dto.CreateCategoryDto;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.service.CategoryCommandService;
import com.mybudget.transactions.service.CategoryQueryService;
import com.mybudget.transactions.service.TransactionQueryService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryCommandService categoryCommandService;

    @MockBean
    private CategoryQueryService categoryQueryService;

    @MockBean
    private TransactionQueryService transactionQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String subject = "test-subject";

    @Nested
    @DisplayName("POST /api/categories")
    class CreateCategoryTests {

        @Test
        void shouldCreateCategory() throws Exception {
            // GIVEN
            CreateCategoryDto dto = new CreateCategoryDto();
            dto.setCategoryName("Test");
            dto.setTransactionType(TransactionType.EXPENSE);

            // WHEN
            var result = mockMvc.perform(post("/api/categories")
                    .with(jwt().jwt(b -> b.claim("sub", subject)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(CategoryConstants.STATUS_201))
                    .andExpect(jsonPath("$.statusMsg").value(CategoryConstants.MESSAGE_201));

            verify(categoryCommandService, times(1))
                    .createUserCategory(eq("Test"), eq(TransactionType.EXPENSE));
        }
    }

    @Nested
    @DisplayName("GET /api/categories/{categoryId}")
    class GetCategoryTests {

        @Test
        void shouldReturnCategoryDto() throws Exception {
            // GIVEN
            Category cat = new Category();
            cat.setId(1L);
            cat.setCategoryName("Food");
            cat.setTransactionType(TransactionType.EXPENSE);
            when(categoryQueryService.findUserCategory(1L))
                    .thenReturn(cat);

            // WHEN
            var result = mockMvc.perform(get("/api/categories/1")
                    .with(jwt().jwt(b -> b.claim("sub", subject))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.categoryName").value("Food"))
                    .andExpect(jsonPath("$.transactionType").value("EXPENSE"));
            verify(categoryQueryService, times(1)).findUserCategory(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/categories/type/{transactionType}")
    class GetCategoriesByTypeTests {

        @Test
        void shouldReturnListOfCategories() throws Exception {
            // GIVEN
            Category c1 = new Category(); c1.setId(2L);
            c1.setCategoryName("Salary"); c1.setTransactionType(TransactionType.INCOME);
            Category c2 = new Category(); c2.setId(3L);
            c2.setCategoryName("Gift"); c2.setTransactionType(TransactionType.INCOME);
            List<Category> list = Arrays.asList(c1, c2);
            when(categoryQueryService.findCategoriesByType(TransactionType.INCOME))
                    .thenReturn(list);

            // WHEN
            var result = mockMvc.perform(get("/api/categories/type/INCOME")
                    .with(jwt().jwt(b -> b.claim("sub", subject))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[1].id").value(3));
            verify(categoryQueryService, times(1))
                    .findCategoriesByType(TransactionType.INCOME);
        }
    }

    @Nested
    @DisplayName("GET /api/categories")
    class GetUserCategoriesTests {

        @Test
        void shouldReturnAllUserCategories() throws Exception {
            // GIVEN
            Category c = new Category();
            c.setId(4L);
            c.setCategoryName("Misc");
            c.setTransactionType(TransactionType.EXPENSE);
            when(categoryQueryService.findUserCategories())
                    .thenReturn(List.of(c));

            // WHEN
            var result = mockMvc.perform(get("/api/categories")
                    .with(jwt().jwt(b -> b.claim("sub", subject))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(4));
            verify(categoryQueryService, times(1)).findUserCategories();
        }
    }

    @Nested
    @DisplayName("GET /api/categories/{categoryId}/transactions")
    class GetCategoryTransactionsTests {

        @Test
        void shouldReturnTransactionsForCategory() throws Exception {
            // GIVEN
            Transaction t1 = new Transaction();
            t1.setId(10L);
            t1.setAmount(BigDecimal.valueOf(50));
            t1.setTransactionType(TransactionType.EXPENSE);
            Transaction t2 = new Transaction();
            t2.setId(11L);
            t2.setAmount(BigDecimal.valueOf(75));
            t2.setTransactionType(TransactionType.EXPENSE);
            List<Transaction> txs = Arrays.asList(t1, t2);
            when(transactionQueryService.findByCategory(5L)).thenReturn(txs);

            // WHEN
            var result = mockMvc.perform(get("/api/categories/5/transactions")
                    .with(jwt().jwt(b -> b.claim("sub", subject))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
            verify(transactionQueryService, times(1)).findByCategory(5L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/categories/{categoryId}")
    class DeleteCategoryTests {

        @Test
        void shouldDeleteCategory() throws Exception {
            // GIVEN
            // WHEN
            var result = mockMvc.perform(delete("/api/categories/7")
                    .with(jwt().jwt(b -> b.claim("sub", subject))));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(CategoryConstants.STATUS_200))
                    .andExpect(jsonPath("$.statusMsg").value(CategoryConstants.MESSAGE_200));
            verify(categoryCommandService, times(1)).deleteCategory(7L);
        }
    }
}
