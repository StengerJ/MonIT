package com.monit.server.web;

import com.monit.common.CheckResult;
import com.monit.common.DiskUsage;
import com.monit.common.HealthReport;
import com.monit.server.entity.CheckResultEntity;
import com.monit.server.entity.ClientEntity;
import com.monit.server.entity.MetricEntity;
import com.monit.server.repository.CheckResultRepository;
import com.monit.server.repository.ClientRepository;
import com.monit.server.repository.MetricRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
public class ReportsController {

    private final ClientRepository clientRepository;
    private final MetricRepository metricRepository;
    private final CheckResultRepository checkResultRepository;

    public ReportsController(ClientRepository clientRepository,
                              MetricRepository metricRepository,
                              CheckResultRepository checkResultRepository) {
        this.clientRepository = clientRepository;
        this.metricRepository = metricRepository;
        this.checkResultRepository = checkResultRepository;
    }

    @PostMapping("/api/reports")
    public ResponseEntity<Void> ingest(@RequestBody HealthReport report, HttpServletRequest request) {
        UUID clientId = (UUID) request.getAttribute("clientId");
        Instant time = report.getTimestamp() != null ? report.getTimestamp() : Instant.now();

        double maxDiskPercent = report.getDisks() == null ? 0.0 : report.getDisks().stream()
                .mapToDouble(DiskUsage::getUsedPercent)
                .max()
                .orElse(0.0);

        MetricEntity metric = new MetricEntity();
        metric.setTime(time);
        metric.setClientId(clientId);
        metric.setCpuPercent(report.getCpuPercent());
        metric.setMemPercent(report.getMemPercent());
        metric.setMaxDiskPercent(maxDiskPercent);
        metric.setNetInBytes(report.getNetInBytes());
        metric.setNetOutBytes(report.getNetOutBytes());
        metric.setUptimeSeconds(report.getUptimeSeconds());
        metricRepository.save(metric);

        if (report.getChecks() != null) {
            for (CheckResult check : report.getChecks()) {
                CheckResultEntity entity = new CheckResultEntity();
                entity.setTime(time);
                entity.setClientId(clientId);
                entity.setCheckName(check.getName());
                entity.setCheckType(check.getType());
                entity.setStatus(check.getStatus().name());
                entity.setMessage(check.getMessage());
                checkResultRepository.save(entity);
            }
        }

        ClientEntity client = clientRepository.findById(clientId).orElseThrow();
        client.setLastSeen(time);
        clientRepository.save(client);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
