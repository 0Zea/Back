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
    
    // 인증번호 저장소 (실제 운영에서는 Redis 사용 권장)
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    
    // 만료된 인증번호 정리용 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public VerificationCodeService() {
        // 1분마다 만료된 인증번호 정리
        scheduler.scheduleAtFixedRate(this::cleanExpiredCodes, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 인증번호 저장 (5분 만료)
     */
    public void saveVerificationCode(String email, String code) {
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodes.put(email, verificationCode);
        log.info("인증번호 저장: {} -> {}", email, code);
    }
    
    /**
     * 인증번호 확인
     */
    public boolean verifyCode(String email, String code) {
        VerificationCode savedCode = verificationCodes.get(email);
        if (savedCode == null) {
            return false;
        }
        
        // 만료 확인
        if (LocalDateTime.now().isAfter(savedCode.getExpiryTime())) {
            verificationCodes.remove(email);
            return false;
        }
        
        // 코드 일치 확인
        if (savedCode.getCode().equals(code)) {
            verificationCodes.remove(email); // 사용 후 삭제
            log.info("인증번호 확인 성공: {}", email);
            return true;
        }
        
        return false;
    }
    
    /**
     * 만료된 인증번호 정리
     */
    private void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().getExpiryTime())
        );
    }
    
    /**
     * 인증번호 정보를 담는 내부 클래스
     */
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