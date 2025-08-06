package org.ozea.common.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Log4j2
public class PerformanceAspect {

    private final ConcurrentHashMap<String, AtomicLong> methodCallCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> methodTotalTime = new ConcurrentHashMap<>();

    @Around("execution(* org.ozea..service.*Service.*(..))")
    public Object logServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String key = className + "." + methodName;
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 메트릭 업데이트
            methodCallCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            methodTotalTime.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(duration);
            
            // 성능 로그 (1초 이상 걸리는 경우)
            if (duration > 1000) {
                log.warn("Slow method execution: {} took {}ms", key, duration);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Method {} failed after {}ms", key, endTime - startTime, e);
            throw e;
        }
    }

    @Around("execution(* org.ozea..controller.*Controller.*(..))")
    public Object logControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String key = className + "." + methodName;
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // API 응답 시간 로그
            log.info("API Response: {} took {}ms", key, duration);
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("API Error: {} failed after {}ms", key, endTime - startTime, e);
            throw e;
        }
    }

    // 메트릭 조회 메서드
    public ConcurrentHashMap<String, AtomicLong> getMethodCallCount() {
        return methodCallCount;
    }

    public ConcurrentHashMap<String, AtomicLong> getMethodTotalTime() {
        return methodTotalTime;
    }
} 