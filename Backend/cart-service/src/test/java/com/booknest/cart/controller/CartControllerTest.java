package com.booknest.cart.controller;

import com.booknest.cart.dto.AddToCartRequest;
import com.booknest.cart.dto.CartResponse;
import com.booknest.cart.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Unit Tests (Pure Mockito)")
class CartControllerTest {

    @Mock private CartServiceImpl cartService;

    @InjectMocks
    private CartController cartController;

    private CartResponse sampleCart;

    @BeforeEach
    void setUp() {
        sampleCart = new CartResponse();
        sampleCart.setCartId(1L);
        sampleCart.setUserId(10L);
        sampleCart.setTotalPrice(998.0);
        sampleCart.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("getCart: returns 200")
    void getCart_success() {
        when(cartService.getCartByUser(10L)).thenReturn(sampleCart);

        ResponseEntity<CartResponse> response = cartController.getCart(10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("addItem: returns 200")
    void addItem_success() {
        AddToCartRequest req = new AddToCartRequest();
        req.setBookId(100L);

        when(cartService.addItem(eq(10L), any(AddToCartRequest.class))).thenReturn(sampleCart);

        ResponseEntity<CartResponse> response = cartController.addItem(10L, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("clearCart: returns 200")
    void clearCart_success() {
        doNothing().when(cartService).clearCart(10L);

        ResponseEntity<String> response = cartController.clearCart(10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Cart cleared.");
    }
}

