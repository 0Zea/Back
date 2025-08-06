package org.ozea.common.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Log4j2
public class LogFileWriter {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "application.log";
    private static final Path LOG_PATH = Paths.get(LOG_DIR, LOG_FILE);

    public void writeLog(String message) {
        try {
            // 로그 디렉토리 생성
            createLogDirectory();
            
            // 로그 메시지 작성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logMessage = String.format("[%s] %s%n", timestamp, message);
            
            Files.write(LOG_PATH, logMessage.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
                
        } catch (IOException e) {
            log.error("로그 파일 작성 실패: {}", e.getMessage());
        }
    }
    
    private void createLogDirectory() throws IOException {
        Path logDir = Paths.get(LOG_DIR);
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir);
            log.info("로그 디렉토리 생성: {}", logDir.toAbsolutePath());
        }
    }
    
    public void writeStartupLog() {
        writeLog("🚀 OZEA 애플리케이션 시작");
        writeLog("📊 시스템 정보:");
        writeLog("   - Java 버전: " + System.getProperty("java.version"));
        writeLog("   - OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        writeLog("   - 프로세서: " + Runtime.getRuntime().availableProcessors() + "개");
        writeLog("✅ 로깅 시스템 초기화 완료");
    }
    
    public void writeKakaoLog(String message) {
        writeLog("🔐 카카오 로그인: " + message);
    }
    
    public void writeApiLog(String endpoint, String message) {
        writeLog("🌐 API 호출 [" + endpoint + "]: " + message);
    }
    
    public void writeErrorLog(String error) {
        writeLog("❌ 오류: " + error);
    }
} 