package com.monit.server.repository;

import com.monit.server.entity.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MetricRepository extends JpaRepository<MetricEntity, MetricEntity.MetricId> {

    @Query("select m from MetricEntity m where m.clientId = :clientId order by m.time desc")
    List<MetricEntity> findRecentByClientId(@Param("clientId") UUID clientId);

    MetricEntity findFirstByClientIdOrderByTimeDesc(UUID clientId);
}
