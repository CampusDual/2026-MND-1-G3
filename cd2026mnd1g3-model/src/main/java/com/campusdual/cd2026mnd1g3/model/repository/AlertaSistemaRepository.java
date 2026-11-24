package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.AlertaSistemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertaSistemaRepository extends JpaRepository<AlertaSistemaEntity, Long> {
    List<AlertaSistemaEntity> findByClinicaIdOrderByFechaProgramadaDesc(Long clinicaId);

    @Query("""
            select count(a)
            from AlertaSistemaEntity a
            where a.clinicaId = :clinicaId
              and a.explotacionId = :farmId
              and a.leida = false
            """)
    long countActiveByFarm(@Param("clinicaId") Long clinicaId,
                           @Param("farmId") Long farmId);
}
