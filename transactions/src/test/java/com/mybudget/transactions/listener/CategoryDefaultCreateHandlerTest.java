package com.mybudget.transactions.listener;

import com.mybudget.common.event.category.CategoryDefaultCreateEvent;
import com.mybudget.transactions.listener.Event.CategoryDefaultCreateHandler;
import com.mybudget.transactions.service.CategoryCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryDefaultCreateHandlerTest {

    @Mock
    private CategoryCommandService categoryCommandService;

    @InjectMocks
    private CategoryDefaultCreateHandler handler;

    @Test
    void shouldSeedDefaultCategoriesForUser_whenHandleCalled() {
        // GIVEN
        CategoryDefaultCreateEvent event =
                new CategoryDefaultCreateEvent("sub123", "alice");

        // WHEN
        handler.handle(event);

        // THEN
        verify(categoryCommandService)
                .seedDefaultCategoriesForUser("sub123", "alice");
    }
}
