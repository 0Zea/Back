package org.ozea.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("로그 파일 작성기 테스트")
class LogFileWriterTest {

    @InjectMocks
    private LogFileWriter logFileWriter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 테스트용 로그 디렉토리 설정
        System.setProperty("user.dir", tempDir.toString());
    }

    @Test
    @DisplayName("로그 메시지 작성 테스트")
    void writeLog_ShouldWriteMessageToFile() throws IOException {
        // Given
        String testMessage = "테스트 로그 메시지";

        // When
        logFileWriter.writeLog(testMessage);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains(testMessage));
    }

    @Test
    @DisplayName("시작 로그 작성 테스트")
    void writeStartupLog_ShouldWriteStartupInfo() throws IOException {
        // Given
        // When
        logFileWriter.writeStartupLog();

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        
        // 시작 로그 메시지 확인
        boolean hasStartupMessage = lines.stream()
                .anyMatch(line -> line.contains("🚀 OZEA 애플리케이션 시작"));
        assertTrue(hasStartupMessage);

        boolean hasSystemInfo = lines.stream()
                .anyMatch(line -> line.contains("📊 시스템 정보:"));
        assertTrue(hasSystemInfo);

        boolean hasJavaVersion = lines.stream()
                .anyMatch(line -> line.contains("Java 버전:"));
        assertTrue(hasJavaVersion);
    }

    @Test
    @DisplayName("카카오 로그 작성 테스트")
    void writeKakaoLog_ShouldWriteKakaoMessage() throws IOException {
        // Given
        String kakaoMessage = "카카오 로그인 성공";

        // When
        logFileWriter.writeKakaoLog(kakaoMessage);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        
        boolean hasKakaoMessage = lines.stream()
                .anyMatch(line -> line.contains("🔐 카카오 로그인: " + kakaoMessage));
        assertTrue(hasKakaoMessage);
    }

    @Test
    @DisplayName("API 로그 작성 테스트")
    void writeApiLog_ShouldWriteApiMessage() throws IOException {
        // Given
        String endpoint = "/api/auth/login";
        String message = "로그인 요청";

        // When
        logFileWriter.writeApiLog(endpoint, message);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        
        boolean hasApiMessage = lines.stream()
                .anyMatch(line -> line.contains("🌐 API 호출 [" + endpoint + "]: " + message));
        assertTrue(hasApiMessage);
    }

    @Test
    @DisplayName("에러 로그 작성 테스트")
    void writeErrorLog_ShouldWriteErrorMessage() throws IOException {
        // Given
        String errorMessage = "데이터베이스 연결 실패";

        // When
        logFileWriter.writeErrorLog(errorMessage);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        
        boolean hasErrorMessage = lines.stream()
                .anyMatch(line -> line.contains("❌ 오류: " + errorMessage));
        assertTrue(hasErrorMessage);
    }

    @Test
    @DisplayName("여러 로그 메시지 작성 테스트")
    void writeMultipleLogs_ShouldWriteAllMessages() throws IOException {
        // Given
        String[] messages = {
            "첫 번째 로그 메시지",
            "두 번째 로그 메시지",
            "세 번째 로그 메시지"
        };

        // When
        for (String message : messages) {
            logFileWriter.writeLog(message);
        }

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertTrue(lines.size() >= 3);

        // 각 메시지가 로그에 포함되어 있는지 확인
        for (String message : messages) {
            boolean messageFound = lines.stream()
                    .anyMatch(line -> line.contains(message));
            assertTrue(messageFound, "메시지를 찾을 수 없습니다: " + message);
        }
    }

    @Test
    @DisplayName("로그 디렉토리 자동 생성 테스트")
    void writeLog_ShouldCreateLogDirectoryIfNotExists() throws IOException {
        // Given
        // 로그 디렉토리가 존재하지 않는 상태

        // When
        logFileWriter.writeLog("테스트 메시지");

        // Then
        Path logDir = tempDir.resolve("logs");
        assertTrue(Files.exists(logDir));
        assertTrue(Files.isDirectory(logDir));

        Path logFile = logDir.resolve("application.log");
        assertTrue(Files.exists(logFile));
    }

    @Test
    @DisplayName("타임스탬프 포함 로그 작성 테스트")
    void writeLog_ShouldIncludeTimestamp() throws IOException {
        // Given
        String testMessage = "타임스탬프 테스트";

        // When
        logFileWriter.writeLog(testMessage);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());

        String logLine = lines.get(0);
        // 타임스탬프 형식 확인 (yyyy-MM-dd HH:mm:ss)
        assertTrue(logLine.matches("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] .*"));
    }

    @Test
    @DisplayName("빈 메시지 로그 작성 테스트")
    void writeLog_ShouldHandleEmptyMessage() throws IOException {
        // Given
        String emptyMessage = "";

        // When
        logFileWriter.writeLog(emptyMessage);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("[]"));
    }

    @Test
    @DisplayName("null 메시지 로그 작성 테스트")
    void writeLog_ShouldHandleNullMessage() throws IOException {
        // Given
        String nullMessage = null;

        // When
        logFileWriter.writeLog(nullMessage);

        // Then
        Path logFile = tempDir.resolve("logs").resolve("application.log");
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("null"));
    }
} 