package com.mybudget.transactions.listener.Event;

import com.mybudget.common.event.CategoriesAfterUserDeleteEvent;
import com.mybudget.transactions.service.CategoryCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriesAfterUserDeleteHandler implements EventHandler<CategoriesAfterUserDeleteEvent> {
    private final CategoryCommandService categoryCommandService;
    private static final Logger logger = LoggerFactory.getLogger(CategoriesAfterUserDeleteHandler.class);

    @Override
    public void handle(CategoriesAfterUserDeleteEvent event) {
        logger.info("Transactions: delete all for user={}", event.getUsername());
        categoryCommandService.deleteAllByUsernameAfterDeletingAccount(event.getUsername());
    }
}
