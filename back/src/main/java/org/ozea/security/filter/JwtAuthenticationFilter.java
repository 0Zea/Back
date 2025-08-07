package org.ozea.security.filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.util.LogFileWriter;
import org.ozea.security.util.JwtProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@Component
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProcessor jwtProcessor;
    private final LogFileWriter logFileWriter;
    private final UserDetailsService userDetailsService;
    // 보안이 필요하지 않은 경로들
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/signup",
        "/api/auth/kakao/callback",
        "/api/auth/kakao/callback/test",
        "/api/monitoring/health",
        "/api/monitoring/metrics",
        "/api/monitoring/info",
        "/error",
        "/favicon.ico",
        "/swagger-ui",
        "/v3/api-docs"
    );
    @Autowired
    public JwtAuthenticationFilter(
            JwtProcessor jwtProcessor,
            @Qualifier("kakaoUserDetailsService") UserDetailsService userDetailsService,
            LogFileWriter logFileWriter) {
        this.jwtProcessor = jwtProcessor;
        this.userDetailsService = userDetailsService;
        this.logFileWriter = logFileWriter;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.debug("🔐 JWT 인증 필터 - URI: {}, Method: {}", requestURI, method);
        try {
            // 공개 경로 체크
            if (shouldNotFilter(request)) {
                log.debug("🔓 공개 경로 - 인증 생략: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }
            String token = extractTokenFromRequest(request);
            if (StringUtils.hasText(token)) {
                log.debug("🔍 토큰 추출 성공 - URI: {}", requestURI);
                logFileWriter.writeApiLog(requestURI, "JWT 토큰 검증 시작");
                // 토큰 블랙리스트 확인
                if (jwtProcessor.isTokenBlacklisted(token)) {
                    log.warn("🚫 블랙리스트된 토큰 - URI: {}", requestURI);
                    logFileWriter.writeErrorLog("블랙리스트된 토큰 사용 시도: " + requestURI);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                // 토큰 유효성 검증
                if (jwtProcessor.validateToken(token)) {
                    String username = jwtProcessor.getUsername(token);
                    String tokenType = jwtProcessor.getTokenType(token);
                    log.debug("✅ 토큰 검증 성공 - 사용자: {}, 타입: {}, URI: {}", username, tokenType, requestURI);
                    // Access Token만 인증 허용
                    if ("access".equals(tokenType)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("🔐 인증 설정 완료 - 사용자: {}, URI: {}", username, requestURI);
                        logFileWriter.writeApiLog(requestURI, "JWT 인증 성공 - 사용자: " + username);
                        // 토큰 만료 시간 로깅
                        long remainingTime = jwtProcessor.getRemainingTime(token);
                        if (remainingTime < 300000) { // 5분 미만
                            log.warn("⚠️ 토큰 만료 임박 - 사용자: {}, 남은 시간: {}ms", username, remainingTime);
                            logFileWriter.writeErrorLog("토큰 만료 임박 - 사용자: " + username + ", 남은 시간: " + remainingTime + "ms");
                        }
                    } else {
                        log.warn("❌ 잘못된 토큰 타입 - 타입: {}, URI: {}", tokenType, requestURI);
                        logFileWriter.writeErrorLog("잘못된 토큰 타입: " + tokenType + " - URI: " + requestURI);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                } else {
                    log.warn("❌ 유효하지 않은 토큰 - URI: {}", requestURI);
                    logFileWriter.writeErrorLog("유효하지 않은 토큰 - URI: " + requestURI);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } else {
                log.warn("❌ 토큰이 없음 - URI: {}", requestURI);
                logFileWriter.writeErrorLog("토큰이 없음 - URI: " + requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.error("💥 JWT 인증 필터 오류 - URI: {}, 오류: {}", requestURI, e.getMessage(), e);
            logFileWriter.writeErrorLog("JWT 인증 필터 오류 - URI: " + requestURI + ", 오류: " + e.getMessage());
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
        String requestURI = request.getRequestURI();
        // 공개 경로 체크
        for (String publicPath : PUBLIC_PATHS) {
            if (requestURI.startsWith(publicPath)) {
                return true;
            }
        }
        // OPTIONS 요청은 CORS preflight 요청이므로 통과
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        return false;
    }
}
