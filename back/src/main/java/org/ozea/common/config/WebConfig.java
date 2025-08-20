package org.ozea.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.*;
import java.util.Arrays;

@Slf4j
@Configuration
public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { RootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { ServletConfig.class, SwaggerConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource());
        return new Filter[] { characterEncodingFilter, corsFilter };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.addMapping("/");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        ServletRegistration existing = servletContext.getServletRegistration("prometheusScrapeServlet");
        if (existing == null) {
            try {
                WebApplicationContext wac =
                        WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

                Servlet prometheusServlet = (Servlet) wac.getBean("prometheusScrapeServlet");

                servletContext.addServlet("prometheusScrapeServlet", prometheusServlet)
                        .addMapping("/api/monitoring/prometheus");
            } catch (Exception e) {
                log.warn("Prometheus servlet not registered", e);
            }
        }
    }


    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
