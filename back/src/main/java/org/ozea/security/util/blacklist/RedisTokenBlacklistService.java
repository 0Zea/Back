package org.ozea.security.util.blacklist;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redis;

    private String key(String jti) {
        return "jwt:bl:" + jti;
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Boolean exists = redis.hasKey(key(jti));
        return exists != null && exists;
    }

    @Override
    public void blacklist(String jti, long ttlMs) {
        if (ttlMs <= 0) ttlMs = 1_000; // 최소 1초
        redis.opsForValue().set(key(jti), "1", ttlMs, TimeUnit.MILLISECONDS);
    }
}