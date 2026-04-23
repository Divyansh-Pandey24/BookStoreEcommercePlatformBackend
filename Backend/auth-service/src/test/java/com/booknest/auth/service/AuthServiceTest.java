package com.booknest.auth.service;

import com.booknest.auth.dto.AuthResponse;
import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.RegisterRequest;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.PasswordResetTokenRepository;
import com.booknest.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private PasswordResetTokenRepository tokenRepo;
    @Mock private PasswordResetService emailService;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setEmail("test@booknest.com");
        existingUser.setFullName("Test User");
        existingUser.setPasswordHash("$2a$10$hashedpassword");
        existingUser.setRole("CUSTOMER");
        existingUser.setProvider("LOCAL");
    }

    // ─────────────────────────── REGISTER ───────────────────────────

    @Test
    @DisplayName("register: new email → saves user and returns success message")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("New User");
        req.setEmail("new@booknest.com");
        req.setPassword("Password@123");
        req.setMobile("9876543210");

        when(userRepo.existsByEmail("new@booknest.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashedPwd");
        when(userRepo.save(any(User.class))).thenReturn(existingUser);

        String result = authService.register(req);

        assertThat(result).contains("registered successfully");
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("register: duplicate email → throws RuntimeException")
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@booknest.com");
        req.setPassword("Password@123");

        when(userRepo.existsByEmail("test@booknest.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already registered");

        verify(userRepo, never()).save(any());
    }

    // ─────────────────────────── LOGIN ───────────────────────────

    @Test
    @DisplayName("login: valid credentials → returns AuthResponse with tokens")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@booknest.com");
        req.setPassword("correctPassword");

        when(userRepo.findByEmail("test@booknest.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctPassword", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtService.generateAccessToken(existingUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(existingUser)).thenReturn("refresh-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("login: unknown email → throws RuntimeException")
    void login_userNotFound_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@booknest.com");
        req.setPassword("any");

        when(userRepo.findByEmail("unknown@booknest.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login: Google OAuth user tries password login → throws RuntimeException")
    void login_googleUser_throws() {
        existingUser.setProvider("GOOGLE");
        LoginRequest req = new LoginRequest();
        req.setEmail("test@booknest.com");
        req.setPassword("any");

        when(userRepo.findByEmail("test@booknest.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Google login");
    }

    @Test
    @DisplayName("login: wrong password → throws RuntimeException")
    void login_wrongPassword_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@booknest.com");
        req.setPassword("wrongPassword");

        when(userRepo.findByEmail("test@booknest.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // ─────────────────────────── REFRESH TOKENS ───────────────────────────

    @Test
    @DisplayName("refreshTokens: invalid token → throws RuntimeException")
    void refreshTokens_invalidToken_throws() {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshTokens("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid or expired");
    }

    @Test
    @DisplayName("refreshTokens: valid token → returns new AuthResponse")
    void refreshTokens_success() {
        when(jwtService.isTokenValid("valid-refresh")).thenReturn(true);
        when(jwtService.extractUserId("valid-refresh")).thenReturn("1");
        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(existingUser)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(existingUser)).thenReturn("new-refresh");

        AuthResponse response = authService.refreshTokens("valid-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    // ─────────────────────────── GOOGLE OAUTH ───────────────────────────

    @Test
    @DisplayName("processGoogleUser: new user → creates account and returns AuthResponse")
    void processGoogleUser_newUser_creates() {
        when(userRepo.findByEmail("google@gmail.com")).thenReturn(Optional.empty());
        when(userRepo.save(any(User.class))).thenReturn(existingUser);
        when(jwtService.generateAccessToken(any())).thenReturn("g-access");
        when(jwtService.generateRefreshToken(any())).thenReturn("g-refresh");

        AuthResponse response = authService.processGoogleUser(
                "google@gmail.com", "Google User", "https://pic.url");

        assertThat(response.getAccessToken()).isEqualTo("g-access");
        verify(userRepo, atLeastOnce()).save(any(User.class));
    }

    @Test
    @DisplayName("processGoogleUser: existing user → logs in without creating")
    void processGoogleUser_existingUser_logsIn() {
        existingUser.setProvider("GOOGLE");
        existingUser.setProfilePicture("https://old-pic.url");

        when(userRepo.findByEmail("test@booknest.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(existingUser)).thenReturn("access");
        when(jwtService.generateRefreshToken(existingUser)).thenReturn("refresh");

        AuthResponse response = authService.processGoogleUser(
                "test@booknest.com", "Test User", "https://old-pic.url");

        assertThat(response).isNotNull();
        // Profile picture unchanged → user not re-saved
        verify(userRepo, never()).save(any());
    }

    // ─────────────────────────── GET USER BY ID ───────────────────────────

    @Test
    @DisplayName("getUserById: missing user → throws RuntimeException")
    void getUserById_notFound_throws() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("getUserById: found → returns User entity")
    void getUserById_found_returnsUser() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));

        User result = authService.getUserById(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@booknest.com");
    }
}
