package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.ProtocolEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import java.time.LocalDate;

public interface ProtocolEventRepository extends JpaRepository<ProtocolEventEntity, Long> {
    List<ProtocolEventEntity> findByFarmIdOrderByApplicationDateAsc(Long farmId);
    List<ProtocolEventEntity> findByApplicationDateAndStatus(LocalDate applicationDate, EventStatus status);
}
