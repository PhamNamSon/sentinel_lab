package io.namson.targetapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.otel.OpenTelemetrySpanContext;

@Configuration(proxyBeanMethods = false)
public class PrometheusExemplarConfig {

    @Bean
    SpanContext prometheusSpanContext() {
        return new OpenTelemetrySpanContext();
    }

}
