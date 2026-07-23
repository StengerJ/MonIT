package com.monit.server.repository;

import com.monit.server.entity.AlertStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlertStateRepository extends JpaRepository<AlertStateEntity, AlertStateEntity.AlertStateId> {
    Optional<AlertStateEntity> findByClientIdAndAlertType(UUID clientId, String alertType);
}
