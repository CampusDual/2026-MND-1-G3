package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoAvisoClinico;
import com.campusdual.cd2026mnd1g3.model.entity.AvisoClinicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvisoClinicoRepository extends JpaRepository<AvisoClinicoEntity, Long>,
        JpaSpecificationExecutor<AvisoClinicoEntity> {
    Optional<AvisoClinicoEntity> findByIdAndClinicaId(Long id, Long clinicaId);
    List<AvisoClinicoEntity> findByClinicaIdAndActivoTrueOrderByFechaObjetivoAscIdAsc(Long clinicaId);

    @Modifying
    @Query("""
            UPDATE AvisoClinicoEntity aviso
            SET aviso.activo = false,
                aviso.canceladoPorBaja = true,
                aviso.fechaActualizacion = CURRENT_TIMESTAMP
            WHERE aviso.clinicaId = :clinicaId
              AND aviso.paciente.id = :pacienteId
              AND aviso.estado IN :estados
              AND aviso.activo = true
            """)
    int cancelarPendientesPorBaja(
            @Param("clinicaId") Long clinicaId,
            @Param("pacienteId") Long pacienteId,
            @Param("estados") List<EstadoAvisoClinico> estados);
}
