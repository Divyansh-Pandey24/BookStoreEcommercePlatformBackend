package com.booknest.auth.controller;

import com.booknest.auth.dto.AuthResponse;
import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.RegisterRequest;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.service.AuthService;
import com.booknest.auth.service.PasswordResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests (Pure Mockito)")
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private PasswordResetService passwordResetService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private AuthResponse sampleAuthResponse() {
        return new AuthResponse(
                "access-token-xyz",
                "refresh-token-xyz",
                "CUSTOMER",
                1L,
                "test@booknest.com",
                "Test User",
                null
        );
    }

    @Test
    @DisplayName("register: success → returns 200 with message")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@booknest.com");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn("User registered successfully");

        ResponseEntity<String> response = authController.register(req);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("registered successfully");
    }

    @Test
    @DisplayName("login: success → returns 200 with AuthResponse")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@booknest.com");

        when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse());

        ResponseEntity<AuthResponse> response = authController.login(req);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token-xyz");
    }

    @Test
    @DisplayName("refresh: success → returns 200")
    void refresh_success() {
        when(authService.refreshTokens("valid-token")).thenReturn(sampleAuthResponse());

        ResponseEntity<AuthResponse> response = authController.refresh(Map.of("refreshToken", "valid-token"));

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token-xyz");
    }

    @Test
    @DisplayName("forgotPassword: success → returns status message")
    void forgotPassword_success() {
        when(passwordResetService.forgotPassword("test@booknest.com")).thenReturn("Email sent");

        String response = authController.forgotPassword(Map.of("email", "test@booknest.com"));

        assertThat(response).isEqualTo("Email sent");
    }
}

