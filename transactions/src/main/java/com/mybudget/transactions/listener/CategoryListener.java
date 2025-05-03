package com.mybudget.transactions.listener;

import com.mybudget.common.event.CategoryDefaultCreateEvent;
import com.mybudget.common.kafka.Topics;
import com.mybudget.transactions.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryListener {

    private final CategoryService categoryService;

    private static final Logger logger = LoggerFactory.getLogger(CategoryListener.class);

    @KafkaListener(topics = Topics.QUEUING_CATEGORIES_CREATE_DEFAULT_V1, groupId = "categories-group")
    public void onCreateDefault(CategoryDefaultCreateEvent event) {

        logger.info("Categories: seeding defaults for sub={}, username={}",
                event.getKeycloakSub(), event.getUsername());

        categoryService.seedDefaultCategoriesForUser(
                event.getKeycloakSub(), event.getUsername());
    }
}
