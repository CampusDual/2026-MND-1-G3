package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.ConsultaClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultaClinicaRepository extends JpaRepository<ConsultaClinicaEntity, Long> {
    List<ConsultaClinicaEntity> findByClinicaIdOrderByFechaConsultaDesc(Long clinicaId);
    Optional<ConsultaClinicaEntity> findByIdAndClinicaId(Long id, Long clinicaId);
}
