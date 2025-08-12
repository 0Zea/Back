package org.ozea.ai.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;

// RateLimitFilter.java
@Component
@Order(5)
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private StringRedisTemplate srt;
    private static final Set<String> TARGET = Set.of("/api/ai/");

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws java.io.IOException, javax.servlet.ServletException {
        String path = req.getRequestURI();
        boolean target = TARGET.stream().anyMatch(path::startsWith);
        if (!target) { chain.doFilter(req, res); return; }

        String ip = ofNullable(req.getHeader("X-Forwarded-For")).orElse(req.getRemoteAddr());
        String key = "ratelimit:"+ip+":"+(System.currentTimeMillis()/ (30_000));
        Long cnt = srt.opsForValue().increment(key);
        if (cnt != null && cnt == 1L) srt.expire(key, ofSeconds(30));
        if (cnt != null && cnt > 8) {
            res.setStatus(429);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"Too many requests\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}