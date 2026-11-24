package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long>,
        JpaSpecificationExecutor<AnimalEntity> {
    boolean existsByEarTag(String earTag);
    List<AnimalEntity> findByFarmId(Long farmId);
    List<AnimalEntity> findByFechaFinRetiradaLessThanEqualAndEstadoSanitario(
            java.time.LocalDate date, EstadoSanitario estadoSanitario);
    java.util.Optional<AnimalEntity> findByClinicaIdAndFarmIdAndEarTagIgnoreCase(
            Long clinicaId, Long farmId, String earTag);
    boolean existsByFarmIdAndEarTagIgnoreCase(Long farmId, String earTag);
    List<AnimalEntity> findDistinctByClinicaIdAndFarmIdAndSubtipoIsNotNull(
            Long clinicaId, Long farmId);
    List<AnimalEntity> findDistinctByClinicaIdAndFarmIdAndUbicacionIsNotNull(
            Long clinicaId, Long farmId);
}
