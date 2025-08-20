package org.ozea.security.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class LoginAttemptService {

    private final StringRedisTemplate srt;

    public LoginAttemptService(StringRedisTemplate srt) {
        this.srt = srt;
    }

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lockout-duration:300000}")
    private long lockoutDurationMs;

    private String failKey(String email)  { return "login:fail:" + email; }
    private String lockKey(String email)  { return "login:lock:" + email; }

    public boolean isBlocked(String email) {
        Boolean locked = srt.hasKey(lockKey(email));
        return locked != null && locked;
    }

    public void recordFailedAttempt(String email) {
        String fKey = failKey(email);
        Long c = srt.opsForValue().increment(fKey);
        if (c != null && c == 1L) {
            srt.expire(fKey, Duration.ofMillis(lockoutDurationMs));
        }
        if (c != null && c >= maxAttempts) {
            srt.opsForValue().set(lockKey(email), "1", Duration.ofMillis(lockoutDurationMs));
        }
    }

    public void recordSuccessfulAttempt(String email) {
        srt.delete(failKey(email));
        srt.delete(lockKey(email));
    }

    public int getRemainingAttempts(String email) {
        if (isBlocked(email)) return 0;
        String v = srt.opsForValue().get(failKey(email));
        int used = v == null ? 0 : Integer.parseInt(v);
        int remain = maxAttempts - used;
        return Math.max(remain, 0);
    }
}