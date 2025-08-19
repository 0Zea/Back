package org.ozea.monitoring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PrometheusConfig {

    @Bean
    @Primary
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(io.micrometer.prometheus.PrometheusConfig.DEFAULT);
    }

    @Bean
    public MeterRegistry meterRegistry(
            PrometheusMeterRegistry reg,
            @Value("${APP_ENV:local}") String env,
            @Value("${APP_NAME:ozea-backend}") String app
    ) {
        reg.config().commonTags("app", app, "env", env);

        new ClassLoaderMetrics().bindTo(reg);
        new JvmMemoryMetrics().bindTo(reg);
        new JvmGcMetrics().bindTo(reg);
        new ProcessorMetrics().bindTo(reg);
        new UptimeMetrics().bindTo(reg);
        new FileDescriptorMetrics().bindTo(reg);
        try { new Log4j2Metrics().bindTo(reg); } catch (Throwable ignore) {}
        return reg;
    }
}