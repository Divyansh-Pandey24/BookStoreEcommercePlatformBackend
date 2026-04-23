package com.booknest.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.booknest.order.dto.UserProfileDto;

// Feign client for cross-service communication with the auth-service
@FeignClient(name = "AUTH-SERVICE")
public interface UserClient {

    // Retrieve the profile details for a specific user
    @GetMapping("/auth//user/{userId}")
    UserProfileDto getUserProfile(@PathVariable("userId") Long userId);
}