package com.monit.server.repository;

import com.monit.server.entity.CheckResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckResultRepository extends JpaRepository<CheckResultEntity, Long> {
    List<CheckResultEntity> findTop20ByClientIdOrderByTimeDesc(UUID clientId);
}
