package org.ozea.common.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class SimpleCacheManager {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SimpleCacheManager() {
        // 5분마다 만료된 캐시 정리
        scheduler.scheduleAtFixedRate(this::cleanExpiredEntries, 5, 5, TimeUnit.MINUTES);
    }

    public void put(String key, Object value, long ttlSeconds) {
        long expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(key, new CacheEntry(value, expiryTime));
        log.debug("Cache put: {} with TTL: {}s", key, ttlSeconds);
    }

    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit: {}", key);
            return entry.getValue();
        }
        
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
            log.debug("Cache expired: {}", key);
        }
        
        log.debug("Cache miss: {}", key);
        return null;
    }

    public void remove(String key) {
        cache.remove(key);
        log.debug("Cache removed: {}", key);
    }

    public void clear() {
        cache.clear();
        log.info("Cache cleared");
    }

    private void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("Cache cleanup completed. Remaining entries: {}", cache.size());
    }

    public int size() {
        return cache.size();
    }

    private static class CacheEntry {
        private final Object value;
        private final long expiryTime;

        public CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
} 