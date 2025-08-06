package org.ozea.common.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Log4j2
public class JdbcLoggingAspect {

    @Around("execution(* org.ozea..mapper.*Mapper.*(..))")
    public Object logJdbcOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String key = className + "." + methodName;
        
        log.info("=== JDBC Operation Start ===");
        log.info("Mapper: {}, Method: {}", className, methodName);
        log.info("Parameters: {}", Arrays.toString(joinPoint.getArgs()));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("JDBC operation completed in {}ms", duration);
            log.info("Result: {}", result != null ? result.getClass().getSimpleName() : "null");
            log.info("=== JDBC Operation End ===");
            
            // 느린 쿼리 경고 (100ms 이상)
            if (duration > 100) {
                log.warn("Slow JDBC operation: {} took {}ms", key, duration);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("JDBC operation failed after {}ms: {}", endTime - startTime, e.getMessage());
            log.error("=== JDBC Operation End with Error ===");
            throw e;
        }
    }

    @Around("execution(* org.ozea..service.*Service.*(..))")
    public Object logServiceOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String key = className + "." + methodName;
        
        log.info("=== Service Operation Start ===");
        log.info("Service: {}, Method: {}", className, methodName);
        log.info("Parameters: {}", Arrays.toString(joinPoint.getArgs()));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("Service operation completed in {}ms", duration);
            log.info("=== Service Operation End ===");
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Service operation failed after {}ms: {}", endTime - startTime, e.getMessage());
            log.error("=== Service Operation End with Error ===");
            throw e;
        }
    }
} 