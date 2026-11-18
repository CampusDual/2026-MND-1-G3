package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.MedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicamentoRepository extends JpaRepository<MedicamentoEntity, Long> {
    List<MedicamentoEntity> findByClinicaId(Long clinicaId);
}
