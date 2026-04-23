package com.booknest.order.controller;

import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.service.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests (Pure Mockito)")
class OrderControllerTest {

    @Mock private OrderServiceImpl orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderResponse sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new OrderResponse();
        sampleOrder.setOrderId(1L);
        sampleOrder.setUserId(10L);
        sampleOrder.setTotalAmount(998.0);
        sampleOrder.setOrderStatus("CONFIRMED");
        sampleOrder.setPlacedAt(LocalDateTime.now());
        sampleOrder.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("placeOrder: returns 201")
    void placeOrder_success() {
        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setDeliveryAddress("123 Main St");

        when(orderService.placeOrder(eq(10L), any(PlaceOrderRequest.class))).thenReturn(sampleOrder);

        ResponseEntity<OrderResponse> response = orderController.placeOrder(10L, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody().getOrderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getOrderById: returns 200")
    void getOrderById_success() {
        when(orderService.getOrderById(1L, 10L, "CUSTOMER")).thenReturn(sampleOrder);

        ResponseEntity<OrderResponse> response = orderController.getOrderById(1L, 10L, "CUSTOMER");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("getMyOrders: returns list")
    void getMyOrders_success() {
        when(orderService.getMyOrders(10L)).thenReturn(List.of(sampleOrder));

        ResponseEntity<List<OrderResponse>> response = orderController.getMyOrders(10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("cancelOrder: returns 200")
    void cancelOrder_success() {
        when(orderService.cancelOrder(1L, 10L)).thenReturn(sampleOrder);

        ResponseEntity<OrderResponse> response = orderController.cancelOrder(1L, 10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateStatus: returns 200")
    void updateStatus_success() {
        when(orderService.updateOrderStatus(1L, "SHIPPED", "ADMIN")).thenReturn(sampleOrder);

        ResponseEntity<OrderResponse> response = orderController.updateStatus(1L, Map.of("status", "SHIPPED"), "ADMIN");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}
