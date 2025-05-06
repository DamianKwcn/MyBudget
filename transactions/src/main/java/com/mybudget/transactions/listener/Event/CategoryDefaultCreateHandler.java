package com.mybudget.transactions.listener.Event;

import com.mybudget.common.event.CategoryDefaultCreateEvent;
import com.mybudget.transactions.service.CategoryCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDefaultCreateHandler implements EventHandler<CategoryDefaultCreateEvent> {
    private final CategoryCommandService categoryCommandService;
    private static final Logger logger = LoggerFactory.getLogger(CategoryDefaultCreateHandler.class);

    @Override
    public void handle(CategoryDefaultCreateEvent event) {
        logger.info("Categories: seeding defaults for sub={}, username={}",
                event.getKeycloakSub(), event.getUsername());
        categoryCommandService.seedDefaultCategoriesForUser(
                event.getKeycloakSub(), event.getUsername());
    }
}
