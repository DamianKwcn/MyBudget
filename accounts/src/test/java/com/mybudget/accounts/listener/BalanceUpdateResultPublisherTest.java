package com.mybudget.accounts.listener;

import com.mybudget.accounts.entity.User;
import com.mybudget.accounts.listener.Event.BalanceUpdateResultPublisher;
import com.mybudget.accounts.repository.UserRepository;
import com.mybudget.common.event.BalanceUpdateRequestedEvent;
import com.mybudget.common.event.BalanceUpdateResultEvent;
import com.mybudget.common.enums.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mybudget.common.kafka.Topics.STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceUpdateResultPublisherTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private BalanceUpdateResultPublisher publisher;

    private final BalanceUpdateRequestedEvent event =
            new BalanceUpdateRequestedEvent("sub", 1L, BigDecimal.valueOf(5), "INCOME");

    @Test
    void shouldPublishWhenUserExists() {
        // GIVEN
        User user = new User();
        user.setKeycloakSub("sub");
        user.setBalance(BigDecimal.TEN);
        when(userRepository.findByKeycloakSub("sub"))
                .thenReturn(Optional.of(user));
        ArgumentCaptor<BalanceUpdateResultEvent> captor =
                ArgumentCaptor.forClass(BalanceUpdateResultEvent.class);

        // WHEN
        publisher.publish(event, TransactionStatus.SUCCESS, BigDecimal.valueOf(15));

        // THEN
        verify(kafkaTemplate).send(
                eq(STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1),
                captor.capture()
        );
        BalanceUpdateResultEvent sent = captor.getValue();
        assertEquals("sub", sent.getKeycloakSub());
        assertEquals(1L, sent.getTransactionId());
        assertEquals(0, sent.getBalanceAfter().compareTo(BigDecimal.valueOf(15)));
        assertEquals(0, sent.getAmount().compareTo(BigDecimal.valueOf(5)));
        assertEquals("INCOME", sent.getTransactionType());
        assertEquals(TransactionStatus.SUCCESS, sent.getStatus());
    }

    @Test
    void shouldNotPublishWhenUserMissing() {
        // GIVEN
        when(userRepository.findByKeycloakSub("sub"))
                .thenReturn(Optional.empty());

        // WHEN
        publisher.publish(event, TransactionStatus.FAILED, BigDecimal.ZERO);

        // THEN
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void shouldFallbackToUserBalanceWhenUpdatedBalanceNull() {
        // GIVEN
        User user = new User();
        user.setKeycloakSub("sub");
        user.setBalance(BigDecimal.valueOf(20));
        when(userRepository.findByKeycloakSub("sub"))
                .thenReturn(Optional.of(user));
        ArgumentCaptor<BalanceUpdateResultEvent> captor =
                ArgumentCaptor.forClass(BalanceUpdateResultEvent.class);

        // WHEN
        publisher.publish(event, TransactionStatus.SUCCESS, null);

        // THEN
        verify(kafkaTemplate).send(
                eq(STREAMING_ACCOUNTS_BALANCE_UPDATE_RESULT_V1),
                captor.capture()
        );
        assertEquals(0, captor.getValue().getBalanceAfter().compareTo(BigDecimal.valueOf(20)));
    }
}
