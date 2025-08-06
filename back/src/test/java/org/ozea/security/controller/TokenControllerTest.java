package org.ozea.security.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.util.LogFileWriter;
import org.ozea.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("토큰 컨트롤러 테스트")
class TokenControllerTest {

    @Mock
    private JwtProcessor jwtProcessor;

    @Mock
    private LogFileWriter logFileWriter;

    @InjectMocks
    private TokenController tokenController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshToken_ShouldReturnNewTokens() {
        // Given
        String refreshToken = "valid.refresh.token";
        String username = "test@example.com";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";

        request.addHeader("Authorization", "Bearer " + refreshToken);

        when(jwtProcessor.validateToken(refreshToken)).thenReturn(true);
        when(jwtProcessor.getTokenType(refreshToken)).thenReturn("refresh");
        when(jwtProcessor.getUsername(refreshToken)).thenReturn(username);
        when(jwtProcessor.generateAccessToken(username)).thenReturn(newAccessToken);
        when(jwtProcessor.generateRefreshToken(username)).thenReturn(newRefreshToken);

        // When
        ResponseEntity<?> response = tokenController.refreshToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());
        assertEquals("토큰이 성공적으로 갱신되었습니다.", apiResponse.getMessage());

        verify(logFileWriter).writeApiLog("/api/auth/token/refresh", "토큰 갱신 요청");
        verify(logFileWriter).writeApiLog("/api/auth/token/refresh", "토큰 갱신 성공 - 사용자: " + username);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Authorization 헤더 없음")
    void refreshToken_ShouldFailWhenNoAuthorizationHeader() {
        // Given
        // Authorization 헤더 없음

        // When
        ResponseEntity<?> response = tokenController.refreshToken(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Refresh Token이 필요합니다.", apiResponse.getMessage());

        verify(logFileWriter).writeErrorLog("Refresh Token이 제공되지 않음");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 잘못된 토큰")
    void refreshToken_ShouldFailWhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtProcessor.validateToken(invalidToken)).thenReturn(false);

        // When
        ResponseEntity<?> response = tokenController.refreshToken(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("유효하지 않은 Refresh Token입니다.", apiResponse.getMessage());

        verify(logFileWriter).writeErrorLog("유효하지 않은 Refresh Token");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 잘못된 토큰 타입")
    void refreshToken_ShouldFailWhenWrongTokenType() {
        // Given
        String accessToken = "access.token";
        request.addHeader("Authorization", "Bearer " + accessToken);

        when(jwtProcessor.validateToken(accessToken)).thenReturn(true);
        when(jwtProcessor.getTokenType(accessToken)).thenReturn("access");

        // When
        ResponseEntity<?> response = tokenController.refreshToken(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Refresh Token이 아닙니다.", apiResponse.getMessage());

        verify(logFileWriter).writeErrorLog("잘못된 토큰 타입: access");
    }

    @Test
    @DisplayName("토큰 검증 성공 테스트")
    void validateToken_ShouldReturnValidResult() {
        // Given
        String accessToken = "valid.access.token";
        String username = "test@example.com";
        request.addHeader("Authorization", "Bearer " + accessToken);

        when(jwtProcessor.validateToken(accessToken)).thenReturn(true);
        when(jwtProcessor.getTokenType(accessToken)).thenReturn("access");
        when(jwtProcessor.getUsername(accessToken)).thenReturn(username);

        // When
        ResponseEntity<?> response = tokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());
        assertEquals("토큰이 유효합니다.", apiResponse.getMessage());

        verify(logFileWriter).writeApiLog("/api/auth/token/validate", "토큰 검증 요청");
        verify(logFileWriter).writeApiLog("/api/auth/token/validate", "토큰 검증 성공 - 사용자: " + username);
    }

    @Test
    @DisplayName("토큰 검증 실패 - 유효하지 않은 토큰")
    void validateToken_ShouldFailWhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtProcessor.validateToken(invalidToken)).thenReturn(false);

        // When
        ResponseEntity<?> response = tokenController.validateToken(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("유효하지 않은 Access Token입니다.", apiResponse.getMessage());

        verify(logFileWriter).writeErrorLog("유효하지 않은 Access Token");
    }

    @Test
    @DisplayName("토큰 정보 조회 성공 테스트")
    void getTokenInfo_ShouldReturnTokenInfo() {
        // Given
        String token = "valid.token";
        String username = "test@example.com";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtProcessor.validateToken(token)).thenReturn(true);
        when(jwtProcessor.getUsername(token)).thenReturn(username);
        when(jwtProcessor.getTokenType(token)).thenReturn("access");

        // When
        ResponseEntity<?> response = tokenController.getTokenInfo(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());
        assertEquals("토큰 정보를 조회했습니다.", apiResponse.getMessage());

        verify(logFileWriter).writeApiLog("/api/auth/token/info", "토큰 정보 조회 요청");
        verify(logFileWriter).writeApiLog("/api/auth/token/info", "토큰 정보 조회 성공 - 사용자: " + username);
    }

    @Test
    @DisplayName("토큰 정보 조회 실패 - 유효하지 않은 토큰")
    void getTokenInfo_ShouldFailWhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtProcessor.validateToken(invalidToken)).thenReturn(false);

        // When
        ResponseEntity<?> response = tokenController.getTokenInfo(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("유효하지 않은 토큰입니다.", apiResponse.getMessage());
    }

    @Test
    @DisplayName("예외 처리 테스트")
    void refreshToken_ShouldHandleException() {
        // Given
        String token = "valid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtProcessor.validateToken(anyString())).thenThrow(new RuntimeException("Test exception"));

        // When
        ResponseEntity<?> response = tokenController.refreshToken(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("토큰 갱신 중 오류가 발생했습니다.", apiResponse.getMessage());

        verify(logFileWriter).writeErrorLog("토큰 갱신 중 오류: Test exception");
    }
} 