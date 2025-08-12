package org.ozea.common.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimiter {
    private final StringRedisTemplate redis;

    private static final String LUA =
            "local c = redis.call('INCR', KEYS[1]); " +
                    "if c == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]); end; " +
                    "if c > tonumber(ARGV[2]) then return 0 else return 1 end;";

    public boolean allow(String key, Duration window, int limit) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA, Long.class);
        Long ok = redis.execute(script, List.of(key),
                String.valueOf(window.toMillis()),
                String.valueOf(limit));
        return ok != null && ok == 1L;
    }
}