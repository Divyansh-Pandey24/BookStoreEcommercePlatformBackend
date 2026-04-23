package com.booknest.review.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.booknest.review.dto.OrderResponse;

// Feign client for interacting with the order-service to verify purchases
@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {

    // Retrieve order history for the authenticated user
    @GetMapping("/orders/my")
    List<OrderResponse> getMyOrders(@RequestHeader("X-User-Id") Long userId);
}