package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.PacienteClinicaEntity;
import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PacienteClinicaRepository extends JpaRepository<PacienteClinicaEntity, Long>,
        JpaSpecificationExecutor<PacienteClinicaEntity> {
    Optional<PacienteClinicaEntity> findByIdAndClinicaIdAndActivoTrue(Long id, Long clinicaId);
    Optional<PacienteClinicaEntity> findByClinicaIdAndMicrochip(Long clinicaId, String microchip);
    boolean existsByClinicaIdAndPropietarioIdAndNombreNormalizadoAndEspecie(
            Long clinicaId, Long propietarioId, String nombreNormalizado, EspecieMascota especie);
    List<PacienteClinicaEntity> findByClinicaIdAndPropietarioIdAndActivoTrue(Long clinicaId, Long propietarioId);
    List<PacienteClinicaEntity> findByClinicaIdAndEspecieAndRazaIsNotNullAndActivoTrue(
            Long clinicaId, EspecieMascota especie);
    long countByClinicaIdAndPropietarioIdAndActivoTrue(Long clinicaId, Long propietarioId);
    Optional<PacienteClinicaEntity> findByClinicaIdAndNombreIgnoreCaseAndActivoTrue(
            Long clinicaId, String nombre);
}
