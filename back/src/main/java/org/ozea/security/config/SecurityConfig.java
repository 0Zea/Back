package org.ozea.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 설정을 담당하는 클래스.
 * 웹 보안, 인증, 인가 등을 설정합니다.
 */
@Configuration
@EnableWebSecurity // Spring Security를 활성화합니다.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 사용자 정보를 가져오는 UserDetailsService를 빈으로 등록합니다.
     * @return KakaoUserDetailsService 객체
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new KakaoUserDetailsService(); // KakaoUserDetailsService 인스턴스 반환
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

    /**
     * AuthenticationManager를 빈으로 등록하여 다른 곳에서 주입받아 사용할 수 있도록 합니다.
     * @return AuthenticationManager 객체
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 정적 리소스에 대한 보안을 무시하도록 설정합니다.
     * @param web WebSecurity 객체
     * @throws Exception 설정 중 예외 발생 시
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // static resources, ignoring security
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * HTTP 요청에 대한 보안을 설정합니다.
     * @param http HttpSecurity 객체
     * @throws Exception 설정 중 예외 발생 시
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests() // 요청에 대한 접근 권한을 설정합니다.
                .antMatchers("/callback", "/login").permitAll() // /callback, /login 경로는 모두에게 허용합니다.
                .anyRequest().authenticated() // 그 외의 모든 요청은 인증된 사용자만 접근 가능합니다.
                .and()
            .formLogin() // 폼 기반 로그인을 활성화합니다.
                .loginPage("/login") // 커스텀 로그인 페이지 경로를 설정합니다.
                .permitAll()
                .and()
            .logout() // 로그아웃을 활성화합니다.
                .permitAll();

        http.csrf().disable(); // CSRF 보호 기능을 비활성화합니다. (개발 편의를 위해, 운영 환경에서는 활성화를 권장합니다.)
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
        return NoOpPasswordEncoder.getInstance();
    }
}
