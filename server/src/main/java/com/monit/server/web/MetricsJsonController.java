package com.monit.server.web;

import com.monit.server.entity.MetricEntity;
import com.monit.server.repository.MetricRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class MetricsJsonController {

    private final MetricRepository metricRepository;

    public MetricsJsonController(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @GetMapping("/api/clients/{id}/metrics.json")
    public List<MetricEntity> recentMetrics(@PathVariable("id") UUID id) {
        return metricRepository.findRecentByClientId(id);
    }
}
