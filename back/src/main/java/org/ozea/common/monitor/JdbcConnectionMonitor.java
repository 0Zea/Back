package org.ozea.common.monitor;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.lang.reflect.Field;
@Component
@Log4j2
public class JdbcConnectionMonitor {
    @Autowired
    private DataSource dataSource;
    @Scheduled(fixedRate = 30000) // Run every 30 seconds (more frequent monitoring)
    public void monitorConnectionPool() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            try {
                int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
                int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
                int threadsAwaitingConnection = hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
                log.info("=== JDBC Connection Pool Status ===");
                log.info("Total Connections: {}", totalConnections);
                log.info("Active Connections: {}", activeConnections);
                log.info("Idle Connections: {}", idleConnections);
                log.info("Threads Awaiting Connection: {}", threadsAwaitingConnection);
                double utilization = totalConnections > 0 ? (activeConnections * 100.0 / totalConnections) : 0;
                log.info("Connection Pool Utilization: {:.1f}%", utilization);
                if (threadsAwaitingConnection > 0) {
                    log.warn("⚠️ Threads waiting for database connection: {}", threadsAwaitingConnection);
                }
                if (utilization > 80) {
                    log.warn("⚠️ High connection pool utilization: {:.1f}%", utilization);
                }
                if (activeConnections == totalConnections && totalConnections > 0) {
                    log.warn("⚠️ All connections are active - consider increasing pool size");
                }
                if (activeConnections > 0) {
                    log.info("Active connection ratio: {}/{} ({:.1f}%)",
                        activeConnections, totalConnections, utilization);
                }
            } catch (Exception e) {
                log.error("Failed to monitor connection pool: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ DataSource is not HikariDataSource: {}", dataSource.getClass().getSimpleName());
        }
    }
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void logConnectionPoolConfig() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            log.info("=== JDBC Connection Pool Configuration ===");
            log.info("Maximum Pool Size: {}", hikariDataSource.getMaximumPoolSize());
            log.info("Minimum Idle: {}", hikariDataSource.getMinimumIdle());
            log.info("Connection Timeout: {}ms", hikariDataSource.getConnectionTimeout());
            log.info("Idle Timeout: {}ms", hikariDataSource.getIdleTimeout());
            log.info("Max Lifetime: {}ms", hikariDataSource.getMaxLifetime());
            log.info("Leak Detection Threshold: {}ms", hikariDataSource.getLeakDetectionThreshold());
            log.info("=== Recommendations ===");
            if (hikariDataSource.getMaximumPoolSize() < 10) {
                log.info("💡 Consider increasing maximum pool size for better performance");
            }
            if (hikariDataSource.getConnectionTimeout() < 30000) {
                log.info("💡 Consider increasing connection timeout for stability");
            }
        }
    }
    @Scheduled(fixedRate = 60000) // Run every 1 minute
    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        log.info("=== Memory Usage ===");
        log.info("Used Memory: {}MB", usedMemory / (1024 * 1024));
        log.info("Free Memory: {}MB", freeMemory / (1024 * 1024));
        log.info("Total Memory: {}MB", totalMemory / (1024 * 1024));
        log.info("Max Memory: {}MB", maxMemory / (1024 * 1024));
        double memoryUsagePercent = (usedMemory * 100.0) / maxMemory;
        log.info("Memory Usage: {:.1f}%", memoryUsagePercent);
        if (memoryUsagePercent > 80) {
            log.warn("⚠️ High memory usage: {:.1f}%", memoryUsagePercent);
        }
    }
}