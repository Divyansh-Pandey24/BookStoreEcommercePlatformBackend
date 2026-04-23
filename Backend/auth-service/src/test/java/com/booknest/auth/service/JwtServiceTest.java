package com.booknest.auth.service;

import com.booknest.auth.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set properties that are usually injected by Spring
        ReflectionTestUtils.setField(jwtService, "secret", "ThisIsAVeryLongSecretKeyUsedForJwtSigningInTests1234567890!");
        ReflectionTestUtils.setField(jwtService, "accessExpiry", 900000L); // 15 mins
        ReflectionTestUtils.setField(jwtService, "refreshExpiry", 604800000L); // 7 days

        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("test@booknest.com");
        mockUser.setRole("CUSTOMER");
        mockUser.setFullName("Test User");
    }

    @Test
    void generateAccessToken_createsValidTokenWithClaims() {
        String token = jwtService.generateAccessToken(mockUser);
        assertThat(token).isNotBlank();

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role")).isEqualTo("CUSTOMER");
        assertThat(claims.get("email")).isEqualTo("test@booknest.com");
    }

    @Test
    void generateRefreshToken_createsValidToken() {
        String token = jwtService.generateRefreshToken(mockUser);
        assertThat(token).isNotBlank();

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role")).isNull(); // refresh token shouldn't have role
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateAccessToken(mockUser);
        boolean isValid = jwtService.isTokenValid(token);
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        boolean isValid = jwtService.isTokenValid("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature");
        assertThat(isValid).isFalse();
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtService.generateAccessToken(mockUser);
        String role = jwtService.extractRole(token);
        assertThat(role).isEqualTo("CUSTOMER");
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = jwtService.generateAccessToken(mockUser);
        String id = jwtService.extractUserId(token);
        assertThat(id).isEqualTo("1");
    }
}
