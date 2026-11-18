package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.AlertaSistemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaSistemaRepository extends JpaRepository<AlertaSistemaEntity, Long> {
    List<AlertaSistemaEntity> findByClinicaIdOrderByFechaProgramadaDesc(Long clinicaId);
}
