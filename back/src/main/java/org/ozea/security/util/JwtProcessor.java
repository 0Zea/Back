package org.ozea.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Log4j2
public class JwtProcessor {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:3600000}")
    private long tokenExpiration;
    
    @Value("${jwt.refresh-expiration:86400000}")
    private long refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String generateAccessToken(String username) {
        return generateToken(username, tokenExpiration, "access");
    }
    
    // Refresh Token 생성
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshExpiration, "refresh");
    }
    
    private String generateToken(String username, long expiration, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("username", String.class);
        } catch (JwtException e) {
            log.error("JWT 토큰에서 사용자명 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("type", String.class);
        } catch (JwtException e) {
            log.error("JWT 토큰 타입 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
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
            log.error("JWT 서명 검증 실패: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있음: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
