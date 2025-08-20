package org.ozea.monitoring.web;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component("prometheusScrapeServlet")
public class PrometheusScrapeServlet extends HttpServlet {
    @Resource
    private PrometheusMeterRegistry registry;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(200);
        resp.setContentType("text/plain; version=0.0.4; charset=utf-8");
        resp.getWriter().write(registry.scrape());
    }
}