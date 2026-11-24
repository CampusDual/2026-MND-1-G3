package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.RecetaClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaClinicaRepository extends JpaRepository<RecetaClinicaEntity, Long> {
    List<RecetaClinicaEntity> findByClinicaIdOrderByFechaEmisionDesc(Long clinicaId);
}
