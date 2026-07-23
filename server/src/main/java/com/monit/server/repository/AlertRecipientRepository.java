package com.monit.server.repository;

import com.monit.server.entity.AlertRecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AlertRecipientRepository extends JpaRepository<AlertRecipientEntity, UUID> {

    List<AlertRecipientEntity> findByClientIdIsNull();

    List<AlertRecipientEntity> findByClientId(UUID clientId);

    @Query("select r from AlertRecipientEntity r where r.clientId is null or r.clientId = :clientId")
    List<AlertRecipientEntity> findGlobalAndForClient(@Param("clientId") UUID clientId);
}
