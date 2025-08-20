package org.ozea.common.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SlidingWindowRateLimiter {
    private final StringRedisTemplate redis;

    public boolean allow(String key, long windowMs, int limit) {
        final long now = System.currentTimeMillis();
        final long start = now - windowMs;
        Long size = redis.execute((RedisCallback<Long>) conn -> {
            byte[] k = key.getBytes(StandardCharsets.UTF_8);
            conn.zRemRangeByScore(k, Double.NEGATIVE_INFINITY, (double) start);
            conn.zAdd(k, (double) now, String.valueOf(now).getBytes(StandardCharsets.UTF_8));
            conn.pExpire(k, windowMs);
            return conn.zCard(k);
        });
        return size != null && size <= limit;
    }
}