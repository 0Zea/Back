package org.ozea.config;

import lombok.extern.slf4j.Slf4j;
import org.ozea.security.config.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

/**
 * web.xml을 대체하는 Web Application Initializer 클래스.
 * DispatcherServlet과 관련된 설정을 담당합니다.
 */
@Slf4j
@Configuration
public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

//    @Value("${upload.maxFileSize}") Long maxFileSize;

    /**
     * Root Application Context에 사용될 설정 클래스를 지정합니다.
     * @return RootConfig와 SecurityConfig 클래스 배열
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { RootConfig.class, SecurityConfig.class };
    }

    /**
     * Servlet Application Context에 사용될 설정 클래스를 지정합니다.
     * @return ServletConfig 클래스 배열
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { ServletConfig.class };
    }

    /**
     * DispatcherServlet이 처리할 요청 경로를 지정합니다.
     * @return "/" (모든 요청)
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    /**
     * 서블릿 필터를 설정합니다.
     * 여기서는 POST 요청의 body 부분의 문자 인코딩을 UTF-8로 설정합니다.
     * @return CharacterEncodingFilter 배열
     */
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();

        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        DelegatingFilterProxy securityFilterChain = new DelegatingFilterProxy("springSecurityFilterChain");

        return new Filter[] {
                characterEncodingFilter,
                securityFilterChain // 🔥 이게 반드시 있어야 Security 작동함};
        };
    }
}
