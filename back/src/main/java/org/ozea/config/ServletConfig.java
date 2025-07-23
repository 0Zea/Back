package org.ozea.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Spring MVC의 Servlet 관련 설정을 담당하는 클래스.
 * 웹과 관련된 컨트롤러, 뷰 리졸버, 리소스 핸들러 등을 설정합니다.
 */
@EnableWebMvc // Spring MVC를 활성화합니다.
@ComponentScan(basePackages = {
        "org.ozea.controller",
}) // org.ozea.controller 패키지 내의 컴포넌트들을 스캔하여 빈으로 등록합니다.
public class ServletConfig  implements WebMvcConfigurer {

    /**
     * 정적 리소스(css, js, images 등)의 경로를 설정합니다.
     * @param registry ResourceHandlerRegistry 객체
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/**") // /resources/로 시작하는 모든 요청을 처리합니다.
                .addResourceLocations("/resources/"); // /resources/ 경로의 파일들을 제공합니다.
    }

    /**
     * View Resolver를 설정하여 컨트롤러가 반환하는 뷰 이름을 실제 뷰 파일로 변환합니다.
     * @param registry ViewResolverRegistry 객체
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();

        bean.setViewClass(JstlView.class); // JSTL을 사용하는 뷰 클래스를 설정합니다.
        bean.setPrefix("/WEB-INF/views/"); // 뷰 파일의 접두사를 설정합니다.
        bean.setSuffix(".jsp"); // 뷰 파일의 접미사를 설정합니다.

        registry.viewResolver(bean);
    }
}
