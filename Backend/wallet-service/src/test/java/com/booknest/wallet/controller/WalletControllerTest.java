package com.booknest.wallet.controller;

import com.booknest.wallet.dto.RazorpayOrderRequest;
import com.booknest.wallet.dto.RazorpayOrderResponse;
import com.booknest.wallet.dto.WalletDto;
import com.booknest.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletController Unit Tests (Pure Mockito)")
class WalletControllerTest {

    @Mock private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private WalletDto sampleWallet;

    @BeforeEach
    void setUp() {
        // Correct WalletDto uses a parameterized constructor (userId, currentBalance)
        sampleWallet = new WalletDto(10L, 500.0);
    }

    @Test
    @DisplayName("getMyWallet: returns 200")
    void getMyWallet_success() {
        when(walletService.getWallet(10L)).thenReturn(sampleWallet);

        ResponseEntity<WalletDto> response = walletController.getMyWallet(10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getCurrentBalance()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("createOrder: returns 200")
    void createOrder_success() throws Exception {
        // Correct RazorpayOrderResponse uses a parameterized constructor (orderId, key, amount, currency)
        RazorpayOrderResponse razorResponse = new RazorpayOrderResponse("rzp_123", "key_456", 200.0, "INR");
        
        RazorpayOrderRequest razorRequest = new RazorpayOrderRequest();
        razorRequest.setAmount(200.0);

        when(walletService.createRazorpayOrder(200.0)).thenReturn(razorResponse);

        ResponseEntity<RazorpayOrderResponse> response = walletController.createOrder(10L, razorRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getRazorpayOrderId()).isEqualTo("rzp_123");
    }

    @Test
    @DisplayName("deductMoney: returns 200")
    void deductMoney_success() {
        doNothing().when(walletService).deductMoney(eq(10L), anyDouble(), isNull());

        ResponseEntity<Void> response = walletController.deductMoney(10L, 50.0, null);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}
