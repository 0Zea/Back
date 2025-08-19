package org.ozea.common.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class IdempotencyFilter implements Filter {

    private final StringRedisTemplate srt;
    // 추가/교체
    private static final String[] TARGETS = {
            // 결제/포인트/정산
            "/api/point/add", "/api/point/withdraw",
            "/api/coupon/issue", "/api/coupon/use",
            "/api/settlement/close", "/api/settlement/adjust",

            // 인증/계정
            "/api/auth/signup", "/api/auth/reset-password",
            "/api/user/email-change",

            // 퀴즈 제출
            "/api/quiz/submit",

            "/api/notice/create", "/api/notice/update", "/api/notice/delete",
            "/api/banner/save", "/api/banner/delete",
            "/api/product/save", "/api/product/delete"
    };

    private boolean needCheck(String uri, String method) {
        if (!("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))) {
            return false;
        }
        for (String t : TARGETS) {
            if (uri.startsWith(t)) return true;
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        if (!needCheck(uri, req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String idem = req.getHeader("Idempotency-Key");
        if (idem == null || idem.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        String key = "idem:" + uri + ":" + idem;
        Boolean ok = srt.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
        if (Boolean.TRUE.equals(ok)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(409);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"중복 요청입니다(Idempotency-Key).\"}");
        }
    }
}