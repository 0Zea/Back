package org.ozea.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ozea.common.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API 응답 테스트")
class ApiResponseTest {

    @Test
    @DisplayName("성공 응답 생성 테스트")
    void success_ShouldCreateSuccessResponse() {
        // Given
        String data = "테스트 데이터";
        String message = "성공 메시지";

        // When
        ApiResponse<String> response = ApiResponse.success(data, message);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("성공 응답 생성 테스트 - 메시지 없음")
    void success_ShouldCreateSuccessResponseWithoutMessage() {
        // Given
        String data = "테스트 데이터";

        // When
        ApiResponse<String> response = ApiResponse.success(data);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("에러 응답 생성 테스트")
    void error_ShouldCreateErrorResponse() {
        // Given
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        String message = "인증 실패";

        // When
        ApiResponse<Void> response = ApiResponse.error(errorCode, message);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(errorCode.getCode(), response.getErrorCode());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("에러 응답 생성 테스트 - 기본 메시지")
    void error_ShouldCreateErrorResponseWithDefaultMessage() {
        // Given
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // When
        ApiResponse<Void> response = ApiResponse.error(errorCode);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(errorCode.getCode(), response.getErrorCode());
        assertEquals(errorCode.getMessage(), response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("복잡한 데이터 타입 응답 테스트")
    void success_ShouldHandleComplexData() {
        // Given
        TestData testData = new TestData("테스트", 123);

        // When
        ApiResponse<TestData> response = ApiResponse.success(testData, "복잡한 데이터 성공");

        // Then
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals("복잡한 데이터 성공", response.getMessage());
        assertEquals("테스트", response.getData().getName());
        assertEquals(123, response.getData().getValue());
    }

    @Test
    @DisplayName("null 데이터 응답 테스트")
    void success_ShouldHandleNullData() {
        // Given
        // When
        ApiResponse<String> response = ApiResponse.success(null, "null 데이터 테스트");

        // Then
        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("null 데이터 테스트", response.getMessage());
    }

    @Test
    @DisplayName("여러 에러 코드 테스트")
    void error_ShouldHandleDifferentErrorCodes() {
        // Given
        ErrorCode[] errorCodes = {
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.EXTERNAL_API_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR
        };

        // When & Then
        for (ErrorCode errorCode : errorCodes) {
            ApiResponse<Void> response = ApiResponse.error(errorCode);
            
            assertFalse(response.isSuccess());
            assertEquals(errorCode.getCode(), response.getErrorCode());
            assertEquals(errorCode.getMessage(), response.getMessage());
        }
    }

    @Test
    @DisplayName("타임스탬프 생성 테스트")
    void response_ShouldHaveValidTimestamp() {
        // Given
        long beforeCreation = System.currentTimeMillis();

        // When
        ApiResponse<String> response = ApiResponse.success("테스트");

        // Then
        long afterCreation = System.currentTimeMillis();
        assertNotNull(response.getTimestamp());
        
        // LocalDateTime을 Instant로 변환하여 비교
        long timestampMillis = response.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(timestampMillis >= beforeCreation);
        assertTrue(timestampMillis <= afterCreation);
    }

    @Test
    @DisplayName("응답 동등성 테스트")
    void response_ShouldBeEqualWithSameData() {
        // Given
        String data = "테스트 데이터";
        String message = "테스트 메시지";

        // When
        ApiResponse<String> response1 = ApiResponse.success(data, message);
        ApiResponse<String> response2 = ApiResponse.success(data, message);

        // Then
        assertEquals(response1.getData(), response2.getData());
        assertEquals(response1.getMessage(), response2.getMessage());
        assertEquals(response1.isSuccess(), response2.isSuccess());
    }

    // 테스트용 내부 클래스
    private static class TestData {
        private String name;
        private int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return value == testData.value && name.equals(testData.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + value;
        }
    }
} 