package com.booknest.notification.controller;

import com.booknest.notification.dto.NotificationResponse;
import com.booknest.notification.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests (Pure Mockito)")
class NotificationControllerTest {

    @Mock private NotificationServiceImpl notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse sampleNotify;

    @BeforeEach
    void setUp() {
        sampleNotify = new NotificationResponse();
        sampleNotify.setNotificationId(1L);
        sampleNotify.setUserId(10L);
        sampleNotify.setMessage("Test message");
        sampleNotify.setIsRead(false);
        sampleNotify.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAll: returns 200 with list")
    void getAll_success() {
        when(notificationService.getNotifications(10L)).thenReturn(List.of(sampleNotify));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getAll(10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("markAsRead: returns 200 with notification")
    void markAsRead_success() {
        when(notificationService.markAsRead(1L, 10L)).thenReturn(sampleNotify);

        ResponseEntity<NotificationResponse> response = notificationController.markAsRead(1L, 10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getNotificationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("delete: returns 204 No Content")
    void delete_success() {
        doNothing().when(notificationService).deleteNotification(1L, 10L);

        ResponseEntity<Void> response = notificationController.delete(1L, 10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }
}
