package com.monit.server.alert;

import com.monit.server.entity.AlertStateEntity;
import com.monit.server.repository.AlertStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlertStateService {

    private final AlertStateRepository alertStateRepository;

    public AlertStateService(AlertStateRepository alertStateRepository) {
        this.alertStateRepository = alertStateRepository;
    }

    public boolean recordAndCheckTransition(UUID clientId, String alertType, String newState) {
        Optional<AlertStateEntity> existing = alertStateRepository.findByClientIdAndAlertType(clientId, alertType);

        boolean changed = existing.isEmpty() || !existing.get().getCurrentState().equals(newState);

        AlertStateEntity entity = existing.orElseGet(AlertStateEntity::new);
        entity.setClientId(clientId);
        entity.setAlertType(alertType);
        entity.setCurrentState(newState);
        if (changed) {
            entity.setLastNotifiedAt(Instant.now());
        }
        alertStateRepository.save(entity);

        return changed;
    }
}
