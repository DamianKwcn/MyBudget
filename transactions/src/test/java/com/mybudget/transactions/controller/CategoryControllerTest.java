package com.mybudget.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.transactions.constants.TransactionConstants;
import com.mybudget.transactions.dto.CreateCategoryDto;
import com.mybudget.transactions.entity.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String subject = "test-subject";

    @Test
    void shouldCreateCategory() throws Exception {
        // GIVEN
        CreateCategoryDto createCategoryDTO = new CreateCategoryDto();
        createCategoryDTO.setCategoryName("Test");
        createCategoryDTO.setTransactionType(TransactionType.EXPENSE);

        // WHEN
        var resultActions = mockMvc.perform(post("/api/categories")
                .with(jwt().jwt(builder -> builder.claim("sub", subject)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCategoryDTO)));

        // THEN
        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(TransactionConstants.STATUS_201))
                .andExpect(jsonPath("$.statusMsg").value(TransactionConstants.MESSAGE_201));

        verify(categoryService, times(1))
                .createUserCategory(eq(subject), eq("Test"), eq(TransactionType.EXPENSE));
    }
}
