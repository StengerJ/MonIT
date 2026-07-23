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
@Table(name = "alert_state")
@IdClass(AlertStateEntity.AlertStateId.class)
@Data
@NoArgsConstructor
public class AlertStateEntity {

    @Id
    @Column(name = "client_id")
    private UUID clientId;

    @Id
    @Column(name = "alert_type")
    private String alertType;

    @Column(name = "current_state", nullable = false)
    private String currentState;

    @Column(name = "last_notified_at")
    private Instant lastNotifiedAt;

    public static class AlertStateId implements Serializable {
        private UUID clientId;
        private String alertType;

        public AlertStateId() {
        }

        public AlertStateId(UUID clientId, String alertType) {
            this.clientId = clientId;
            this.alertType = alertType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AlertStateId)) return false;
            AlertStateId that = (AlertStateId) o;
            return Objects.equals(clientId, that.clientId) && Objects.equals(alertType, that.alertType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, alertType);
        }
    }
}
