package com.booknest.order.client;

import com.booknest.order.dto.WalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Feign client for cross-service communication with the wallet-service
@FeignClient(name = "WALLET-SERVICE")
public interface WalletClient {

    // Fetch the wallet details for a specific user
    @GetMapping("/wallet/{userId}")
    WalletDto getWallet(@PathVariable Long userId);

    // Deduct a specified amount from the user's wallet balance
    @PostMapping("/wallet/{userId}/deduct")
    void deductMoney(@PathVariable Long userId, @RequestParam Double amount);

    // Refund or add a specified amount to the user's wallet balance
    @PostMapping("/wallet/{userId}/add")
    void addMoney(@PathVariable Long userId, @RequestParam Double amount);
}