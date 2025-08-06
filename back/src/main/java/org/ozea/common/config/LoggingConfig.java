package org.ozea.common.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableScheduling
@Log4j2
public class LoggingConfig {

    @PostConstruct
    public void init() {
        // 로그 디렉토리 생성
        createLogDirectory();
        
        log.info("🚀 OZEA 애플리케이션 시작");
        log.info("📊 시스템 정보:");
        log.info("   - Java 버전: {}", System.getProperty("java.version"));
        log.info("   - JVM 벤더: {}", System.getProperty("java.vendor"));
        log.info("   - OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("   - 프로세서: {}개", Runtime.getRuntime().availableProcessors());

        // 메모리 정보
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();

        log.info("💾 메모리 정보:");
        log.info("   - 최대 힙: {}MB", maxHeap / (1024 * 1024));
        log.info("   - 사용 힙: {}MB", usedHeap / (1024 * 1024));
        log.info("   - 힙 사용률: {:.1f}%", (usedHeap * 100.0) / maxHeap);

        // 스레드 정보
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        log.info("🧵 스레드 정보:");
        log.info("   - 총 스레드: {}", threadBean.getThreadCount());
        log.info("   - 데몬 스레드: {}", threadBean.getDaemonThreadCount());

        log.info("✅ 로깅 시스템 초기화 완료");
        
        // 로그 파일 생성 확인
        checkLogFile();
    }
    
    private void createLogDirectory() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                log.info("📁 로그 디렉토리 생성: {}", logDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("❌ 로그 디렉토리 생성 실패: {}", e.getMessage());
        }
    }
    
    private void checkLogFile() {
        try {
            Path logFile = Paths.get("logs/application.log");
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
                log.info("📄 로그 파일 생성: {}", logFile.toAbsolutePath());
            }
            
            // 테스트 로그 메시지 작성
            String testMessage = String.format("[%s] INFO: 로그 파일 테스트 메시지\n", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            Files.write(logFile, testMessage.getBytes(), java.nio.file.StandardOpenOption.APPEND);
            log.info("✅ 로그 파일 테스트 메시지 작성 완료");
            
        } catch (IOException e) {
            log.error("❌ 로그 파일 생성 실패: {}", e.getMessage());
        }
    }
} 