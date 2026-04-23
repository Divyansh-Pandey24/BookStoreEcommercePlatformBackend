package com.booknest.cart.client;

import com.booknest.cart.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign client for cross-service communication with the book-service
@FeignClient(name = "BOOK-SERVICE")
public interface BookClient {

    // Retrieve detailed book information from the book-service
    @GetMapping("/books/{bookId}")
    BookResponse getBookById(@PathVariable Long bookId);
}