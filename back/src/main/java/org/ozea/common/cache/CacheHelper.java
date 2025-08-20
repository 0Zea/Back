package org.ozea.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.util.UUID.randomUUID;

@Component
@RequiredArgsConstructor
public class CacheHelper {
    private final RedisTemplate<String, Object> redis;
    private static final String UNLOCK_LUA =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "  return redis.call('DEL', KEYS[1]) " +
                    "else return 0 end";

    public boolean tryLock(String key, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    public String tryLockToken(String key, Duration ttl) {
        String token = randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    public boolean unlock(String key, String token) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
        Long r = (Long) redis.execute(script, java.util.Collections.singletonList(key), token);
        return r != null && r == 1L;
    }

    public void unlock(String key) {
        redis.delete(key);
    }
}