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
public class LoggingAspect {

    @Around("execution(* org.ozea..service.*Service.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.info("=== Service Method Start ===");
        log.info("Class: {}, Method: {}", className, methodName);
        log.info("Parameters: {}", Arrays.toString(joinPoint.getArgs()));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            
            log.info("Method completed successfully in {}ms", endTime - startTime);
            log.info("=== Service Method End ===");
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Method failed after {}ms with error: {}", endTime - startTime, e.getMessage());
            log.error("=== Service Method End with Error ===");
            throw e;
        }
    }

    @Around("execution(* org.ozea..controller.*Controller.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.info("=== Controller Method Start ===");
        log.info("Class: {}, Method: {}", className, methodName);
        log.info("Parameters: {}", Arrays.toString(joinPoint.getArgs()));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            
            log.info("Controller method completed in {}ms", endTime - startTime);
            log.info("=== Controller Method End ===");
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Controller method failed after {}ms", endTime - startTime, e);
            log.error("=== Controller Method End with Error ===");
            throw e;
        }
    }
} 