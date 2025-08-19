package org.ozea.security.filter;

import lombok.extern.log4j.Log4j2;
import org.ozea.common.util.LogFileWriter;
import org.ozea.security.config.SecurityConfig;
import org.ozea.security.util.JwtProcessor;
import org.ozea.security.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProcessor jwtProcessor;
    private final LogFileWriter logFileWriter;
    private final CustomUserDetailsService userDetailsService;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtProcessor jwtProcessor,
            CustomUserDetailsService userDetailsService,
            LogFileWriter logFileWriter
    ) {
        this.jwtProcessor = jwtProcessor;
        this.userDetailsService = userDetailsService;
        this.logFileWriter = logFileWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        log.debug("🔐 JWT 인증 필터 - URI: {}, Method: {}", requestURI, method);

        try {
            if (shouldNotFilter(request)) {
                log.debug("🔓 공개 경로 - 인증 생략: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            final String token = extractTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                log.warn("❌ 토큰이 없음 - URI: {}", requestURI);
                logFileWriter.writeErrorLog("토큰이 없음 - URI: " + requestURI);
                response.setHeader("WWW-Authenticate",
                        "Bearer error=\"invalid_token\", error_description=\"missing\"");
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            log.debug("🔍 토큰 추출 성공 - URI: {}", requestURI);
            logFileWriter.writeApiLog(requestURI, "JWT 토큰 검증 시작");

            if (jwtProcessor.isTokenBlacklisted(token)) {
                log.warn("🚫 블랙리스트된 토큰 - URI: {}", requestURI);
                logFileWriter.writeErrorLog("블랙리스트된 토큰 사용 시도: " + requestURI);
                response.setHeader("WWW-Authenticate",
                        "Bearer error=\"invalid_token\", error_description=\"blacklisted\"");
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (jwtProcessor.validateToken(token)) {
                final String username = jwtProcessor.getUsername(token);
                final String tokenType = jwtProcessor.getTokenType(token);

                if (!"access".equals(tokenType)) {
                    log.warn("❌ 잘못된 토큰 타입 - 타입: {}, URI: {}", tokenType, requestURI);
                    logFileWriter.writeErrorLog("잘못된 토큰 타입: " + tokenType + " - URI: " + requestURI);
                    response.setHeader("WWW-Authenticate",
                            "Bearer error=\"invalid_token\", error_description=\"wrong_type\"");
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("🔐 인증 설정 완료 - 사용자: {}, URI: {}", username, requestURI);
                logFileWriter.writeApiLog(requestURI, "JWT 인증 성공 - 사용자: " + username);

                long remainingTime = jwtProcessor.getRemainingTime(token);
                if (remainingTime < 300_000) {
                    try {
                        String freshToken = jwtProcessor.generateAccessToken(username);
                        response.setHeader("X-New-Token", freshToken);
                        log.debug("♻️ 토큰 재발급 헤더 첨부 - 사용자: {}, 남은(ms): {}", username, remainingTime);
                    } catch (Exception reissueEx) {
                        log.warn("⚠️ 토큰 재발급 중 예외 - 사용자: {}, 사유: {}", username, reissueEx.getMessage());
                    }
                }

            } else {
                String typ = jwtProcessor.getTokenType(token);
                log.warn("❌ 유효하지 않은 토큰(type={}) - URI: {}", typ, requestURI);
                logFileWriter.writeErrorLog("유효하지 않은 토큰(type=" + typ + ") - URI: " + requestURI);
                response.setHeader("WWW-Authenticate",
                        "Bearer error=\"invalid_token\", error_description=\"expired_or_invalid\"");
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        } catch (Exception e) {
            log.error("💥 JWT 인증 필터 오류 - URI: {}, 오류: {}", requestURI, e.getMessage(), e);
            logFileWriter.writeErrorLog("JWT 인증 필터 오류 - URI: " + requestURI + ", 오류: " + e.getMessage());
            response.setHeader("WWW-Authenticate",
                    "Bearer error=\"server_error\", error_description=\"jwt_filter_exception\"");
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("/".equals(uri)) return true;
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        for (String pattern : SecurityConfig.PUBLIC_ENDPOINTS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}