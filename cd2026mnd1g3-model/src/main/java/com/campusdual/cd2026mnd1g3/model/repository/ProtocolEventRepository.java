package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.ProtocolEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProtocolEventRepository extends JpaRepository<ProtocolEventEntity, Long> {
    List<ProtocolEventEntity> findByFarmIdOrderByApplicationDateAsc(Long farmId);
    List<ProtocolEventEntity> findByApplicationDateAndStatus(LocalDate applicationDate, EventStatus status);

    @Query("""
            select count(e)
            from ProtocolEventEntity e
            where e.farm.clinicaId = :clinicaId
              and e.farm.id = :farmId
              and e.status = com.campusdual.cd2026mnd1g3.api.enums.EventStatus.PROGRAMADO
            """)
    long countPendingByFarm(@Param("clinicaId") Long clinicaId,
                            @Param("farmId") Long farmId);
}
