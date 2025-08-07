package org.ozea.common.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.aspect.PerformanceAspect;
import org.ozea.common.cache.SimpleCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Log4j2
public class MonitoringController {
    private final PerformanceAspect performanceAspect;
    private final SimpleCacheManager cacheManager;
    /**
     * 애플리케이션 헬스체크
     *
     * @return 애플리케이션 상태 정보
     *
     * @apiNote
     * - status: "UP" | "DOWN"
     * - timestamp: 현재 시간 (밀리초)
     *
     * @example
     * GET /api/monitoring/health
     * Response: {"status":"UP","timestamp":1754469885812}
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        health.put("environment", "Spring Legacy");
        log.info("Health check requested - Status: UP");
        return ResponseEntity.ok(health);
    }
    /**
     * 애플리케이션 메트릭 정보
     *
     * @return 성능, 메모리, 캐시 메트릭
     *
     * @apiNote
     * - memory: JVM 메모리 사용량
     * - performance: 서비스별 성능 지표
     * - cache: 캐시 상태
     *
     * @example
     * GET /api/monitoring/metrics
     * Response: {
     *   "memory": {"heapUsed": 73918424, "heapMax": 4294967296},
     *   "performance": {"UserServiceImpl.checkEmail": {"callCount": 11, "totalTime": 139}},
     *   "cache": {"size": 0}
     * }
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        // JVM 메트릭
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", memoryBean.getHeapMemoryUsage().getUsed());
        memory.put("heapMax", memoryBean.getHeapMemoryUsage().getMax());
        memory.put("nonHeapUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
        memory.put("heapUsagePercent",
            (memoryBean.getHeapMemoryUsage().getUsed() * 100.0) / memoryBean.getHeapMemoryUsage().getMax());
        metrics.put("memory", memory);
        // 성능 메트릭
        ConcurrentHashMap<String, AtomicLong> callCount = performanceAspect.getMethodCallCount();
        ConcurrentHashMap<String, AtomicLong> totalTime = performanceAspect.getMethodTotalTime();
        Map<String, Object> performance = new HashMap<>();
        callCount.forEach((method, count) -> {
            Map<String, Object> methodMetrics = new HashMap<>();
            methodMetrics.put("callCount", count.get());
            long total = totalTime.getOrDefault(method, new AtomicLong(0)).get();
            methodMetrics.put("totalTime", total);
            methodMetrics.put("averageTime", count.get() > 0 ? total / count.get() : 0);
            performance.put(method, methodMetrics);
        });
        metrics.put("performance", performance);
        // 캐시 메트릭
        Map<String, Object> cache = new HashMap<>();
        cache.put("size", cacheManager.size());
        cache.put("type", "ConcurrentMapCacheManager");
        metrics.put("cache", cache);
        // 시스템 메트릭
        Map<String, Object> system = new HashMap<>();
        system.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        system.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        metrics.put("system", system);
        log.info("Metrics requested - Performance methods: {}", performance.size());
        return ResponseEntity.ok(metrics);
    }
    /**
     * 애플리케이션 정보
     *
     * @return 애플리케이션 기본 정보
     *
     * @apiNote
     * - application: 애플리케이션 이름
     * - version: 버전 정보
     * - framework: 사용 중인 프레임워크
     * - java: Java 버전
     * - uptime: 실행 시간 (밀리초)
     *
     * @example
     * GET /api/monitoring/info
     * Response: {
     *   "application": "OZEA",
     *   "version": "1.0.0",
     *   "framework": "Spring Framework 5.3.37",
     *   "java": "21.0.6",
     *   "uptime": 810270
     * }
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "OZEA");
        info.put("version", "1.0.0");
        info.put("framework", "Spring Framework 5.3.37");
        info.put("java", System.getProperty("java.version"));
        info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        info.put("environment", "Spring Legacy");
        info.put("architecture", "JDBC + MyBatis");
        info.put("cache", "ConcurrentMapCacheManager");
        info.put("monitoring", "Custom AOP-based");
        log.info("Info requested - Application: OZEA v1.0.0");
        return ResponseEntity.ok(info);
    }
    /**
     * 상세 성능 정보
     *
     * @return 서비스별 상세 성능 지표
     *
     * @apiNote
     * 각 서비스의 호출 횟수, 총 실행 시간, 평균 실행 시간을 제공
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> performance() {
        Map<String, Object> performance = new HashMap<>();
        ConcurrentHashMap<String, AtomicLong> callCount = performanceAspect.getMethodCallCount();
        ConcurrentHashMap<String, AtomicLong> totalTime = performanceAspect.getMethodTotalTime();
        Map<String, Object> details = new HashMap<>();
        callCount.forEach((method, count) -> {
            Map<String, Object> methodDetails = new HashMap<>();
            long total = totalTime.getOrDefault(method, new AtomicLong(0)).get();
            long average = count.get() > 0 ? total / count.get() : 0;
            methodDetails.put("callCount", count.get());
            methodDetails.put("totalTime", total);
            methodDetails.put("averageTime", average);
            methodDetails.put("lastCall", System.currentTimeMillis()); // 실제로는 마지막 호출 시간을 추적해야 함
            details.put(method, methodDetails);
        });
        performance.put("services", details);
        performance.put("totalMethods", callCount.size());
        performance.put("totalCalls", callCount.values().stream().mapToLong(AtomicLong::get).sum());
        return ResponseEntity.ok(performance);
    }
}