package org.ozea.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Spring MVC의 Servlet 관련 설정을 담당하는 클래스.
 * 웹과 관련된 컨트롤러, 뷰 리졸버, 리소스 핸들러 등을 설정합니다.
 */
@EnableWebMvc // Spring MVC를 활성화합니다.
@ComponentScan(basePackages = {
        "org.ozea.common.controller",
        "org.ozea.security.controller",
        "org.ozea.user.controller",
        "org.ozea.goal.controller",
        "org.ozea.inquiry.controller",
        "org.ozea.notice.controller",
        "org.ozea.inquiry.controller",
        "org.ozea.asset.controller",
        "org.ozea.product.controller"
}) // 컨트롤러 패키지들을 스캔하여 빈으로 등록합니다.
public class ServletConfig  implements WebMvcConfigurer {

    /**
     * CORS 설정을 추가합니다.
     * @param registry CorsRegistry 객체
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 모든 origin 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false)  // credentials를 false로 변경
                .maxAge(3600);
    }

    /**
     * 정적 리소스(css, js, images 등)의 경로를 설정합니다.
     * @param registry ResourceHandlerRegistry 객체
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");

        // Swagger UI 리소스를 위한 핸들러 설정
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        // Swagger WebJar 리소스 설정
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        // Swagger 리소스 설정
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/v2/api-docs")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();

        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".jsp");

        registry.viewResolver(bean);
    }
} 