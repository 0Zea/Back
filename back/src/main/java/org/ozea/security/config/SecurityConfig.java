package org.ozea.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.filter.JwtAuthenticationFilter;
import org.ozea.security.filter.JwtUsernamePasswordAuthenticationFilter;
import org.ozea.security.handler.LoginFailureHandler;
import org.ozea.security.handler.LoginSuccessHandler;
import org.ozea.security.util.JwtProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정을 담당하는 클래스.
 * 웹 보안, 인증, 인가 등을 설정합니다.
 * JWT 기반 인증을 위해 기존 formLogin 설정을 제거하고, 직접 정의한 필터를 등록함
 */
@Log4j2
@Configuration
@EnableWebSecurity // Spring Security를 활성화합니다.
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // Kakao 기반 유저 조회 서비스 (UserDetailsService 구현체)
    private final KakaoUserDetailsService kakaoUserDetailsService;
    private final LocalUserDetailsService localUserDetailsService;

    public void init() {
        log.info("✅ SecurityConfig 초기화됨");
    }

    /**
     * 사용자 정보를 가져오는 UserDetailsService를 빈으로 등록합니다.
     * @return KakaoUserDetailsService 객체
     */
    @Bean
    public UserDetailsService userDetailsService() {
//        return new KakaoUserDetailsService(); // KakaoUserDetailsService 인스턴스 반환
        return localUserDetailsService; // KakaoUserDetailsService 말고 로컬로 설정
    }

    /**
     * AuthenticationManager를 설정합니다.
     * UserDetailsService와 PasswordEncoder를 사용하여 인증을 처리합니다.
     * @param auth AuthenticationManagerBuilder 객체
     * @throws Exception 설정 중 예외 발생 시
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // static resources, ignoring security
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // CSRF 보호 기능을 비활성화합니다. (개발 편의를 위해, 운영 환경에서는 활성화를 권장합니다.)
            .formLogin().disable() // 기존 form 로그인 사용 안함
            .httpBasic().disable() // 기본 로그인 방식 사용 안함
            .authorizeRequests() // 요청에 대한 접근 권한을 설정합니다.
                .antMatchers("/",
                        "/signup",
                        "/login",
                        "/local-login",
                        "/main",
                        "/additional-info",
                        "/callback",
                        "/goal/**",
                        // 🔽 Swagger 경로 추가
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/webjars/**").permitAll() // 인증 없이 접근 허용
                .antMatchers("/api/auth/**").permitAll() // 회원가입, 로그인 API
                .anyRequest().authenticated() // 그 외의 모든 요청은 인증된 사용자만 접근 가능합니다.
                .and()
                .addFilterAt(jwtUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // 로그인 인증 필터 등록
                .addFilterAfter(jwtAuthenticationFilter(), JwtUsernamePasswordAuthenticationFilter.class); // JWT 토큰 검증 필터 등록 (인증 후 요청마다 실행됨)
    }

    /**
     * PasswordEncoder를 빈으로 등록합니다.
     * 여기서는 시연을 위해 암호화하지 않는 NoOpPasswordEncoder를 사용합니다.
     * 실제 애플리케이션에서는 BCryptPasswordEncoder와 같은 강력한 인코더를 사용해야 합니다.
     * @return PasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // For demonstration purposes, using NoOpPasswordEncoder.
        // In a real application, use a strong password encoder like BCryptPasswordEncoder.
//        return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }

    // 6-1. 사용자 로그인 시 이메일/비밀번호 받아서 인증 시도 → 성공/실패 핸들러 지정
    @Bean
    public JwtUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter() throws Exception {
        JwtUsernamePasswordAuthenticationFilter filter = new JwtUsernamePasswordAuthenticationFilter(authenticationManagerBean(), loginSuccessHandler(), loginFailureHandler());

        filter.setFilterProcessesUrl("/api/auth/login");

        filter.setAuthenticationSuccessHandler(loginSuccessHandler());
        filter.setAuthenticationFailureHandler(loginFailureHandler());

        return filter;
    }

    // 6-2. 로그인 성공 시 JWT 토큰 발급
    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtProcessor());
    }

    // 6-3. 로그인 실패 시 에러 처리
    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    // 7. 매 요청마다 Authorization 헤더에서 JWT를 꺼내 인증 처리
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProcessor(), userDetailsService());
    }

    // 8. JWT 발급 및 검증 유틸 객체 등록
    @Bean
    public JwtProcessor jwtProcessor() {
        return new JwtProcessor(); // JWT 관련 로직 클래스
    }
}
