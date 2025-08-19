package org.ozea.monitoring.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestTimingFilter extends OncePerRequestFilter {

    @Autowired
    private MeterRegistry registry;

    private boolean exclude(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri == null) return true;
        if (uri.startsWith("/api/monitoring/")) return true;
        if (uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs")) return true;
        if (uri.startsWith("/favicon")) return true;
        if (uri.startsWith("/assets") || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (exclude(req)) {
            chain.doFilter(req, res);
            return;
        }
        long start = System.nanoTime();
        try {
            chain.doFilter(req, res);
        } finally {
            long took = System.nanoTime() - start;
            String pattern = (String) req.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String pathTag = (pattern != null) ? pattern : req.getRequestURI();
            Timer.builder("http.server.requests")
                    .description("HTTP server request latency")
                    .tag("method", req.getMethod())
                    .tag("uri", pathTag)
                    .tag("status", Integer.toString(res.getStatus()))
                    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .register(registry)
                    .record(Duration.ofNanos(took));
        }
    }
}