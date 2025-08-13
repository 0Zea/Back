package org.ozea.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ozea.security.filter.AuthenticationErrorFilter;
import org.ozea.security.filter.JwtAuthenticationFilter;
import org.ozea.security.filter.JwtUsernamePasswordAuthenticationFilter;
import org.ozea.security.handler.LoginFailureHandler;
import org.ozea.security.handler.LoginSuccessHandler;
import org.ozea.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationErrorFilter authenticationErrorFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final ObjectMapper objectMapper;

    /**
     * 전역 공개 경로 – 필터/인가 규칙이 동일하게 참조하도록 상수화
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/refresh-token",         // 혹시 기존 프론트가 이 경로도 쓰면 대비
            "/api/auth/kakao/callback",
            "/api/auth/kakao/callback/test",
            "/api/auth/token/refresh",
            "/api/auth/token/validate",
            "/api/auth/token/info",
            "/api/monitoring/health",
            "/api/monitoring/metrics",
            "/api/monitoring/info",
            "/error",
            "/favicon.ico",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtUsernamePasswordAuthenticationFilter jsonLoginFilter(AuthenticationManager authenticationManager) {
        return new JwtUsernamePasswordAuthenticationFilter(
                authenticationManager, loginSuccessHandler, loginFailureHandler, objectMapper
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // ★ 와일드카드 + credentials=TRUE 금지. 반드시 명시적 Origin만 허용
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            // 안전한 기본값(로컬만 허용)
            c.setAllowedOrigins(List.of("http://localhost:5173"));
        } else {
            c.setAllowedOrigins(allowedOrigins);
        }

        c.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        c.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        c.setExposedHeaders(Arrays.asList("Authorization", "X-New-Token"));
        c.setAllowCredentials(true);
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":true,\"message\":\"로그아웃 성공\"}");
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                        })
                )
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/**", "/api/monitoring/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/user/**",
                                "/api/goal/**",
                                "/api/asset/**",
                                "/api/product/**",
                                "/api/quiz/**",
                                "/api/point/**",
                                "/api/inquiry/**",
                                "/api/notice/**",
                                "/api/allaccount/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                );

        // 필터 순서: Error → JsonLogin → JWT
        http.addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAt(
                jsonLoginFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class))),
                UsernamePasswordAuthenticationFilter.class
        );
        http.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }
}