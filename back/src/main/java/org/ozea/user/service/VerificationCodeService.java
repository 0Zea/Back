package org.ozea.user.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class VerificationCodeService {

    private final StringRedisTemplate srt;

    public VerificationCodeService(StringRedisTemplate srt) {
        this.srt = srt;
    }

    private String codeKey(String email)    { return "verify:code:" + email; }
    private String triesKey(String email)   { return "verify:try:"  + email; }
    public void saveVerificationCode(String email, String code) {
        srt.opsForValue().set(codeKey(email), code, 5, TimeUnit.MINUTES);
        srt.delete(triesKey(email));
    }

    public boolean verifyCode(String email, String code) {
        String saved = srt.opsForValue().get(codeKey(email));
        if (saved == null) return false;

        if (saved.equals(code)) {
            srt.delete(codeKey(email));
            srt.delete(triesKey(email));
            return true;
        } else {
            Long c = srt.opsForValue().increment(triesKey(email));
            if (c != null && c == 1L) {
                srt.expire(triesKey(email), Duration.ofMinutes(10));
            }
            if (c != null && c >= 5) {
                srt.delete(codeKey(email));
            }
            return false;
        }
    }
}