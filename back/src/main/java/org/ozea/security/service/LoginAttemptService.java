package org.ozea.security.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class LoginAttemptService {
    
    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${security.login.lockout-duration:300000}")
    private long lockoutDuration;
    
    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();
    
    public boolean isBlocked(String email) {
        LoginAttempt attempt = attempts.get(email);
        if (attempt == null) {
            return false;
        }
        
        if (attempt.isLocked() && !attempt.isExpired()) {
            log.warn("계정 잠금: {}", email);
            return true;
        }
        
        if (attempt.isExpired()) {
            attempts.remove(email);
            return false;
        }
        
        return false;
    }
    
    public void recordFailedAttempt(String email) {
        LoginAttempt attempt = attempts.get(email);
        if (attempt == null) {
            attempt = new LoginAttempt();
            attempts.put(email, attempt);
        }
        
        attempt.incrementFailedAttempts();
        
        if (attempt.getFailedAttempts() >= maxAttempts) {
            attempt.setLocked(true);
            attempt.setLockoutTime(LocalDateTime.now());
            log.warn("계정 잠금 설정: {} (시도 횟수: {})", email, attempt.getFailedAttempts());
        }
    }
    
    public void recordSuccessfulAttempt(String email) {
        attempts.remove(email);
        log.info("로그인 성공: {}", email);
    }
    
    public int getRemainingAttempts(String email) {
        LoginAttempt attempt = attempts.get(email);
        if (attempt == null) {
            return maxAttempts;
        }
        return Math.max(0, maxAttempts - attempt.getFailedAttempts());
    }
    
    private static class LoginAttempt {
        private int failedAttempts = 0;
        private boolean locked = false;
        private LocalDateTime lockoutTime;
        
        public void incrementFailedAttempts() {
            failedAttempts++;
        }
        
        public boolean isExpired() {
            if (lockoutTime == null) {
                return false;
            }
            return LocalDateTime.now().isAfter(lockoutTime.plusSeconds(300)); // 5분
        }
        
        // Getters and Setters
        public int getFailedAttempts() { return failedAttempts; }
        public boolean isLocked() { return locked; }
        public void setLocked(boolean locked) { this.locked = locked; }
        public void setLockoutTime(LocalDateTime lockoutTime) { this.lockoutTime = lockoutTime; }
    }
} 