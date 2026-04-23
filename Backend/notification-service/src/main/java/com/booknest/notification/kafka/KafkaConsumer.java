package com.booknest.notification.kafka;

import com.booknest.notification.dto.OrderEventDto;
import com.booknest.notification.dto.WalletEventDto;
import com.booknest.notification.service.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Consumer component for processing asynchronous events from Kafka topics
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationServiceImpl notificationService;

    // Listen to the order-events topic and process incoming order notifications
    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderEvent(OrderEventDto event) {
        log.info("Received order event: type={}, userId={}, email={}", event.getType(), event.getUserId(), event.getUserEmail());
        notificationService.saveNotification(
            event.getUserId(),
            event.getType(),
            event.getMessage(),
            event.getUserEmail(),
            event.getUserMobile()
        );
    }

    // Listen to the wallet-events topic and process incoming wallet/payment notifications
    @KafkaListener(topics = "wallet-events", groupId = "notification-group")
    public void handleWalletEvent(WalletEventDto event) {
        log.info("Received wallet event: type={}, userId={}, email={}", event.getType(), event.getUserId(), event.getUserEmail());
        notificationService.saveNotification(
            event.getUserId(),
            event.getType(),
            event.getMessage(),
            event.getUserEmail(),
            event.getUserMobile()
        );
    }
}