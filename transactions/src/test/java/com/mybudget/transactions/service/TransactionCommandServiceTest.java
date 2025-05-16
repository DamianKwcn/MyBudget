package com.mybudget.transactions.service;

import com.mybudget.common.event.transaction.TransactionRemovalStartedEvent;
import com.mybudget.common.event.transaction.TransactionSagaStartEvent;
import com.mybudget.common.enums.TransactionStatus;
import com.mybudget.transactions.common.CurrentUserProvider;
import com.mybudget.transactions.entity.Category;
import com.mybudget.transactions.entity.Transaction;
import com.mybudget.transactions.entity.enums.TransactionType;
import com.mybudget.transactions.exception.ResourceNotFoundException;
import com.mybudget.transactions.repository.CategoryRepository;
import com.mybudget.transactions.repository.TransactionRepository;
import com.mybudget.transactions.service.implementation.TransactionCommandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mybudget.common.kafka.Topics.STREAMING_TRANSACTIONS_CREATION_STARTED_V1;
import static com.mybudget.common.kafka.Topics.STREAMING_TRANSACTIONS_REMOVAL_STARTED_V1;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionCommandServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CurrentUserProvider currentUser;

    @InjectMocks
    private TransactionCommandServiceImpl service;

    private final String sub = "testSub";
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(5L);
        category.setKeycloakSub(sub);
        category.setTransactionType(TransactionType.INCOME);
        category.setCategoryBalance(BigDecimal.valueOf(100));
    }

    @Test
    void shouldCreateTransactionAndPublishEvent() {
        // GIVEN
        when(currentUser.getKeycloakSub()).thenReturn(sub);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        // WHEN
        Transaction transaction = service.createTransaction(BigDecimal.valueOf(30), 5L, "note");

        // THEN
        assertNotNull(transaction);
        assertEquals(sub, transaction.getKeycloakSub());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals(BigDecimal.valueOf(30), transaction.getAmount());
        assertEquals(0, category.getCategoryBalance().compareTo(BigDecimal.valueOf(130)));

        ArgumentCaptor<TransactionSagaStartEvent> sagaCaptor =
                ArgumentCaptor.forClass(TransactionSagaStartEvent.class);
        verify(kafkaTemplate).send(eq(STREAMING_TRANSACTIONS_CREATION_STARTED_V1), sagaCaptor.capture());
        TransactionSagaStartEvent sagaEvt = sagaCaptor.getValue();
        assertEquals(sub, sagaEvt.getKeycloakSub());
        assertEquals(0, sagaEvt.getAmount().compareTo(BigDecimal.valueOf(30)));
        assertEquals("INCOME", sagaEvt.getTransactionType());
    }

    @Test
    void shouldThrowResourceNotFoundWhenCategoryMissing() {
        // GIVEN
        when(currentUser.getKeycloakSub()).thenReturn(sub);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () ->
                service.createTransaction(BigDecimal.ONE, 99L, "x")
        );
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void shouldThrowIllegalArgumentWhenCategoryNotOwned() {
        // GIVEN
        Category other = new Category();
        other.setId(6L);
        other.setKeycloakSub("other");
        when(currentUser.getKeycloakSub()).thenReturn(sub);
        when(categoryRepository.findById(6L)).thenReturn(Optional.of(other));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                service.createTransaction(BigDecimal.ONE, 6L, "x")
        );
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void shouldDeleteTransactionAndUpdateCategoryBalance() {
        // GIVEN
        Transaction transaction = new Transaction();
        transaction.setId(42L);
        transaction.setKeycloakSub(sub);
        transaction.setAmount(BigDecimal.valueOf(30));
        transaction.setTransactionType(TransactionType.INCOME);
        transaction.setCategory(category);

        when(currentUser.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findByKeycloakSubAndId(sub, 42L))
                .thenReturn(Optional.of(transaction));

        // WHEN
        service.deleteTransaction(42L);

        // THEN
        ArgumentCaptor<TransactionRemovalStartedEvent> rbCaptor =
                ArgumentCaptor.forClass(TransactionRemovalStartedEvent.class);
        verify(kafkaTemplate).send(eq(STREAMING_TRANSACTIONS_REMOVAL_STARTED_V1), rbCaptor.capture());
        TransactionRemovalStartedEvent rbEvt = rbCaptor.getValue();
        assertEquals(42L, rbEvt.getTransactionId());
        assertEquals(sub, rbEvt.getKeycloakSub());
        assertEquals(0, rbEvt.getAmount().compareTo(BigDecimal.valueOf(30)));
        assertEquals("INCOME", rbEvt.getTransactionType());

        assertEquals(0, category.getCategoryBalance().compareTo(BigDecimal.valueOf(70)));
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowResourceNotFoundWhenTransactionMissing() {
        // GIVEN
        when(currentUser.getKeycloakSub()).thenReturn(sub);
        when(transactionRepository.findByKeycloakSubAndId(sub, 99L))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () ->
                service.deleteTransaction(99L)
        );
        verify(kafkaTemplate, never()).send(any(), any());
        verify(categoryRepository, never()).save(any());
    }
}
