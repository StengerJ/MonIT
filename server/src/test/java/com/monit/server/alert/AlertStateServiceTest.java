package com.monit.server.alert;

import com.monit.server.entity.AlertStateEntity;
import com.monit.server.repository.AlertStateRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertStateServiceTest {

    @Test
    void firstObservationIsATransitionFromImplicitOnline() {
        AlertStateRepository repository = mock(AlertStateRepository.class);
        UUID clientId = UUID.randomUUID();
        when(repository.findByClientIdAndAlertType(clientId, "availability")).thenReturn(Optional.empty());

        AlertStateService service = new AlertStateService(repository);

        boolean changed = service.recordAndCheckTransition(clientId, "availability", "OFFLINE");

        assertThat(changed).isTrue();
        verify(repository).save(any(AlertStateEntity.class));
    }

    @Test
    void sameStateIsNotATransition() {
        AlertStateRepository repository = mock(AlertStateRepository.class);
        UUID clientId = UUID.randomUUID();
        AlertStateEntity existing = new AlertStateEntity();
        existing.setClientId(clientId);
        existing.setAlertType("availability");
        existing.setCurrentState("OFFLINE");
        when(repository.findByClientIdAndAlertType(clientId, "availability")).thenReturn(Optional.of(existing));

        AlertStateService service = new AlertStateService(repository);

        boolean changed = service.recordAndCheckTransition(clientId, "availability", "OFFLINE");

        assertThat(changed).isFalse();
    }

    @Test
    void differentStateIsATransition() {
        AlertStateRepository repository = mock(AlertStateRepository.class);
        UUID clientId = UUID.randomUUID();
        AlertStateEntity existing = new AlertStateEntity();
        existing.setClientId(clientId);
        existing.setAlertType("availability");
        existing.setCurrentState("OFFLINE");
        when(repository.findByClientIdAndAlertType(clientId, "availability")).thenReturn(Optional.of(existing));

        AlertStateService service = new AlertStateService(repository);

        boolean changed = service.recordAndCheckTransition(clientId, "availability", "ONLINE");

        assertThat(changed).isTrue();
    }
}
