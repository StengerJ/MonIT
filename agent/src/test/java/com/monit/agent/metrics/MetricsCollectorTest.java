package com.monit.agent.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsCollectorTest {

    @Test
    void collectsPlausibleSystemMetrics() {
        MetricsCollector collector = new MetricsCollector();

        SystemMetrics metrics = collector.collect();

        assertThat(metrics.getCpuPercent()).isBetween(0.0, 100.0);
        assertThat(metrics.getMemPercent()).isBetween(0.0, 100.0);
        assertThat(metrics.getUptimeSeconds()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.getNetInBytes()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.getNetOutBytes()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.getDisks()).isNotNull();
    }
}
