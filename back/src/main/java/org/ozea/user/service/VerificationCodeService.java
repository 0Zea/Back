package org.ozea.user.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class VerificationCodeService {

    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public VerificationCodeService() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredCodes, 1, 1, TimeUnit.MINUTES);
    }

    public void saveVerificationCode(String email, String code) {
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodes.put(email, verificationCode);

    }

    public boolean verifyCode(String email, String code) {
        VerificationCode savedCode = verificationCodes.get(email);
        if (savedCode == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(savedCode.getExpiryTime())) {
            verificationCodes.remove(email);
            return false;
        }

        if (savedCode.getCode().equals(code)) {
            verificationCodes.remove(email);
    
            return true;
        }
        
        return false;
    }

    private void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().getExpiryTime())
        );
    }

    private static class VerificationCode {
        private final String code;
        private final LocalDateTime expiryTime;
        
        public VerificationCode(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
} 