package org.ozea.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Log4j2
public class JwtProcessor {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:3600000}")
    private long tokenExpiration;
    
    @Value("${jwt.refresh-expiration:86400000}")
    private long refreshExpiration;
    
    @Value("${jwt.issuer:ozea}")
    private String issuer;
    
    @Value("${jwt.audience:ozea-users}")
    private String audience;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성 (보안 강화)
    public String generateAccessToken(String username) {
        return generateToken(username, tokenExpiration, "access", true);
    }
    
    // Refresh Token 생성 (보안 강화)
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshExpiration, "refresh", false);
    }
    
    private String generateToken(String username, long expiration, String type, boolean includeAudience) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        claims.put("username", username);
        claims.put("jti", UUID.randomUUID().toString()); // JWT ID (고유 식별자)
        claims.put("iat", now.getTime() / 1000); // 발급 시간
        claims.put("nbf", now.getTime() / 1000); // Not Before (즉시 사용 가능)
        
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setNotBefore(now)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);
        
        if (includeAudience) {
            builder.setAudience(audience);
        }
        
        return builder.compact();
    }

    public String getUsername(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("username", String.class);
        } catch (JwtException e) {
            log.error("JWT 토큰에서 사용자명 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    public String getTokenType(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (JwtException e) {
            log.error("JWT 토큰 타입 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    public String getJti(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("jti", String.class);
        } catch (JwtException e) {
            log.error("JWT 토큰 ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            Claims claims = getClaimsFromToken(token);
            
            // 토큰 타입 검증
            String tokenType = claims.get("type", String.class);
            if (tokenType == null) {
                log.warn("토큰 타입이 없습니다");
                return false;
            }
            
            // 발급자 검증
            String tokenIssuer = claims.getIssuer();
            if (!issuer.equals(tokenIssuer)) {
                log.warn("잘못된 토큰 발급자: {}", tokenIssuer);
                return false;
            }
            
            // 대상자 검증 (Access Token만)
            if ("access".equals(tokenType)) {
                String tokenAudience = claims.getAudience();
                if (tokenAudience == null || !audience.equals(tokenAudience)) {
                    log.warn("잘못된 토큰 대상자: {}", tokenAudience);
                    return false;
                }
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("JWT 토큰 서명 검증 실패: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰 인수 오류: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("JWT 토큰 처리 중 오류: {}", e.getMessage());
            return false;
        }
    }
    
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("토큰 만료 확인 실패: {}", e.getMessage());
            return true;
        }
    }
    
    public Date getExpirationDate(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (JwtException e) {
            log.error("토큰 만료 시간 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDate(token);
            if (expiration == null) {
                return 0;
            }
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            log.error("토큰 남은 시간 계산 실패: {}", e.getMessage());
            return 0;
        }
    }
    
    // 토큰 블랙리스트 검증 (향후 Redis 연동 가능)
    public boolean isTokenBlacklisted(String token) {
        // TODO: Redis에서 블랙리스트 확인
        return false;
    }
    
    // 토큰 무효화 (로그아웃 시 사용)
    public void blacklistToken(String token) {
        // TODO: Redis에 토큰 추가
        log.info("토큰 블랙리스트 추가: {}", getJti(token));
    }
}
