package org.ozea.monitoring.controller;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MonitoringController {

    @Autowired
    private PrometheusMeterRegistry prometheus;

    @GetMapping(value = "/api/monitoring/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String metrics() {
        return prometheus.scrape();
    }

    @GetMapping(value = "/api/monitoring/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String health() {
        return "{\"status\":\"UP\"}";
    }

    @GetMapping(value = "/api/monitoring/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String info() {
        return "{\"app\":\"ozea-backend\",\"version\":\"1.0.0\"}";
    }
}