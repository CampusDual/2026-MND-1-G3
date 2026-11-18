package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.MovimientoInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventarioEntity, Long> {
    @Query("select coalesce(sum(m.cantidad), 0) from MovimientoInventarioEntity m where m.loteStock.id = :loteStockId")
    double sumByLoteStockId(@Param("loteStockId") Long loteStockId);

    @Query("select coalesce(sum(m.cantidad), 0) from MovimientoInventarioEntity m where m.loteStock.medicamento.id = :medicamentoId")
    double sumByMedicamentoId(@Param("medicamentoId") Long medicamentoId);

    List<MovimientoInventarioEntity> findByClinicaIdOrderByFechaHoraDesc(Long clinicaId);
}
