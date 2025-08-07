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
        createLogDirectory();
        log.info("OZEA Application Started");
        logSystemInfo();
        logMemoryInfo();
        logThreadInfo();
        log.info("Logging System Initialized");
        checkLogFile();
    }
    private void createLogDirectory() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                log.info("Log directory created: {}", logDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create log directory: {}", e.getMessage());
        }
    }
    private void checkLogFile() {
        try {
            Path logFile = Paths.get("logs/application.log");
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
                log.info("Log file created: {}", logFile.toAbsolutePath());
            }
            String testMessage = String.format("[%s] INFO: Log file test message\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            Files.write(logFile, testMessage.getBytes(), java.nio.file.StandardOpenOption.APPEND);
            log.info("Log file test message written");
        } catch (IOException e) {
            log.error("Failed to create log file: {}", e.getMessage());
        }
    }
    private void logSystemInfo() {
        log.info("System Information:");
        log.info("  Java Version: {}", System.getProperty("java.version"));
        log.info("  JVM Vendor: {}", System.getProperty("java.vendor"));
        log.info("  OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("  Processors: {}", Runtime.getRuntime().availableProcessors());
    }
    private void logMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();
        log.info("Memory Information:");
        log.info("  Max Heap: {}MB", maxHeap / (1024 * 1024));
        log.info("  Used Heap: {}MB", usedHeap / (1024 * 1024));
        log.info("  Heap Usage: {:.1f}%", (usedHeap * 100.0) / maxHeap);
    }
    private void logThreadInfo() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        log.info("Thread Information:");
        log.info("  Total Threads: {}", threadBean.getThreadCount());
        log.info("  Daemon Threads: {}", threadBean.getDaemonThreadCount());
    }
}