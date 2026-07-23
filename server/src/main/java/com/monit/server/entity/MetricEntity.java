package com.monit.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "metrics")
@IdClass(MetricEntity.MetricId.class)
@Data
@NoArgsConstructor
public class MetricEntity {

    @Id
    @Column(name = "time")
    private Instant time;

    @Id
    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "cpu_percent", nullable = false)
    private double cpuPercent;

    @Column(name = "mem_percent", nullable = false)
    private double memPercent;

    @Column(name = "max_disk_percent", nullable = false)
    private double maxDiskPercent;

    @Column(name = "net_in_bytes", nullable = false)
    private long netInBytes;

    @Column(name = "net_out_bytes", nullable = false)
    private long netOutBytes;

    @Column(name = "uptime_seconds", nullable = false)
    private long uptimeSeconds;

    public static class MetricId implements Serializable {
        private Instant time;
        private UUID clientId;

        public MetricId() {
        }

        public MetricId(Instant time, UUID clientId) {
            this.time = time;
            this.clientId = clientId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MetricId)) return false;
            MetricId metricId = (MetricId) o;
            return Objects.equals(time, metricId.time) && Objects.equals(clientId, metricId.clientId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, clientId);
        }
    }
}
