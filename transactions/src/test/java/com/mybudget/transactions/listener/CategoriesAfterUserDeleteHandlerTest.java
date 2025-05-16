package com.mybudget.transactions.listener;

import com.mybudget.common.event.category.CategoriesAfterUserDeleteEvent;
import com.mybudget.transactions.listener.Event.CategoriesAfterUserDeleteHandler;
import com.mybudget.transactions.service.CategoryCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoriesAfterUserDeleteHandlerTest {

    @Mock
    private CategoryCommandService categoryCommandService;

    @InjectMocks
    private CategoriesAfterUserDeleteHandler handler;

    @Test
    void shouldDeleteAllByUsernameAfterDeletingAccount_whenHandleCalled() {
        // GIVEN
        CategoriesAfterUserDeleteEvent event = new CategoriesAfterUserDeleteEvent("bob");

        // WHEN
        handler.handle(event);

        // THEN
        verify(categoryCommandService)
                .deleteAllByUsernameAfterDeletingAccount("bob");
    }
}