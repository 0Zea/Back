package org.ozea.ai.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.ozea.common.limiter.SlidingWindowRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RateLimitFilter implements Filter {

    @Autowired
    private SlidingWindowRateLimiter limiter;

    @Autowired
    private MeterRegistry registry;

    private static final Set<String> TARGET = Set.of("/api/ai/");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String route = TARGET.stream().filter(uri::startsWith).findFirst().orElse("default");

        String ip = extractClientIp(req);
        String key = "ratez:" + route + ":" + ip;

        boolean ok = limiter.allow(key, 60_000, 8);
        if (!ok) {
            Counter.builder("http.requests.ratelimit.rejected")
                    .tag("route", route)
                    .register(registry)
                    .increment();
            res.setStatus(429);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"Too many requests\"}");
            return;
        } else {
            Counter.builder("http.requests.ratelimit.allowed")
                    .tag("route", route)
                    .register(registry)
                    .increment();
        }
        chain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest req) {
        String xfwd = req.getHeader("X-Forwarded-For");
        if (xfwd != null && !xfwd.isEmpty()) {
            String[] p = xfwd.split(",");
            if (p.length > 0) {
                return p[0].trim();
            }
        }
        return req.getRemoteAddr();
    }
}