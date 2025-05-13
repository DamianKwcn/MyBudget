package com.mybudget.transactions.listener;

import com.mybudget.common.event.category.CategoryDefaultCreateEvent;
import com.mybudget.transactions.listener.Event.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.mybudget.common.kafka.Topics.QUEUING_CATEGORIES_CREATE_DEFAULT_V1;

@Component
@RequiredArgsConstructor
public class CategoryListener {
    private final EventHandler<CategoryDefaultCreateEvent> defaultCreateHandler;

    @KafkaListener(topics = QUEUING_CATEGORIES_CREATE_DEFAULT_V1, groupId = "categories-group")
    public void onCreateDefault(CategoryDefaultCreateEvent event) {
        defaultCreateHandler.handle(event);
    }
}
