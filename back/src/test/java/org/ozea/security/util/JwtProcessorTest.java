package org.ozea.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 토큰 처리 테스트")
class JwtProcessorTest {

    @InjectMocks
    private JwtProcessor jwtProcessor;

    @BeforeEach
    void setUp() {
        // 테스트용 설정
        ReflectionTestUtils.setField(jwtProcessor, "secretKey", "testSecretKeyForJwtTokenGenerationAndValidation");
        ReflectionTestUtils.setField(jwtProcessor, "tokenExpiration", 3600000L); // 1시간
        ReflectionTestUtils.setField(jwtProcessor, "refreshExpiration", 86400000L); // 24시간
    }

    @Test
    @DisplayName("Access Token 생성 테스트")
    void generateAccessToken_ShouldCreateValidToken() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtProcessor.generateAccessToken(username);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT는 3개 부분으로 구성
        assertEquals(username, jwtProcessor.getUsername(token));
        assertEquals("access", jwtProcessor.getTokenType(token));
        assertTrue(jwtProcessor.validateToken(token));
    }

    @Test
    @DisplayName("Refresh Token 생성 테스트")
    void generateRefreshToken_ShouldCreateValidToken() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtProcessor.generateRefreshToken(username);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
        assertEquals(username, jwtProcessor.getUsername(token));
        assertEquals("refresh", jwtProcessor.getTokenType(token));
        assertTrue(jwtProcessor.validateToken(token));
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출 테스트")
    void getUsername_ShouldExtractUsernameFromToken() {
        // Given
        String username = "test@example.com";
        String token = jwtProcessor.generateAccessToken(username);

        // When
        String extractedUsername = jwtProcessor.getUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("토큰 타입 추출 테스트")
    void getTokenType_ShouldExtractTokenType() {
        // Given
        String username = "test@example.com";
        String accessToken = jwtProcessor.generateAccessToken(username);
        String refreshToken = jwtProcessor.generateRefreshToken(username);

        // When & Then
        assertEquals("access", jwtProcessor.getTokenType(accessToken));
        assertEquals("refresh", jwtProcessor.getTokenType(refreshToken));
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateToken_ShouldReturnTrueForValidToken() {
        // Given
        String username = "test@example.com";
        String token = jwtProcessor.generateAccessToken(username);

        // When
        boolean isValid = jwtProcessor.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("잘못된 토큰 검증 테스트")
    void validateToken_ShouldReturnFalseForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtProcessor.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("빈 토큰 검증 테스트")
    void validateToken_ShouldReturnFalseForEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtProcessor.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("null 토큰 검증 테스트")
    void validateToken_ShouldReturnFalseForNullToken() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtProcessor.validateToken(nullToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Access Token과 Refresh Token 구분 테스트")
    void tokenTypes_ShouldBeDifferent() {
        // Given
        String username = "test@example.com";

        // When
        String accessToken = jwtProcessor.generateAccessToken(username);
        String refreshToken = jwtProcessor.generateRefreshToken(username);

        // Then
        assertNotEquals(accessToken, refreshToken);
        assertEquals("access", jwtProcessor.getTokenType(accessToken));
        assertEquals("refresh", jwtProcessor.getTokenType(refreshToken));
    }

    @Test
    @DisplayName("다른 사용자의 토큰 구분 테스트")
    void differentUsers_ShouldHaveDifferentTokens() {
        // Given
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        // When
        String token1 = jwtProcessor.generateAccessToken(user1);
        String token2 = jwtProcessor.generateAccessToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(user1, jwtProcessor.getUsername(token1));
        assertEquals(user2, jwtProcessor.getUsername(token2));
    }
} 