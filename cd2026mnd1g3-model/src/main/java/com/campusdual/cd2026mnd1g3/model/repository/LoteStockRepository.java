package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.LoteStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteStockRepository extends JpaRepository<LoteStockEntity, Long> {
    List<LoteStockEntity> findByMedicamentoId(Long medicamentoId);
}
