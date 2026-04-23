package com.booknest.notification.kafka;

import com.booknest.notification.dto.OrderEventDto;
import com.booknest.notification.dto.WalletEventDto;
import com.booknest.notification.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private NotificationServiceImpl notificationService;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    private OrderEventDto orderEvent;
    private WalletEventDto walletEvent;

    @BeforeEach
    void setUp() {
        orderEvent = new OrderEventDto(1L, "ORDER_PLACED", "Your order is placed", 1L, "test@booknest.com", "98765");
        walletEvent = new WalletEventDto(1L, "WALLET_CREDIT", "Money added", "test@booknest.com", "98765");
    }

    @Test
    void handleOrderEvent_callsNotificationService() {
        kafkaConsumer.handleOrderEvent(orderEvent);

        verify(notificationService).saveNotification(
                1L, "ORDER_PLACED", "Your order is placed", "test@booknest.com", "98765");
    }

    @Test
    void handleWalletEvent_callsNotificationService() {
        kafkaConsumer.handleWalletEvent(walletEvent);

        verify(notificationService).saveNotification(
                1L, "WALLET_CREDIT", "Money added", "test@booknest.com", "98765");
    }
}
