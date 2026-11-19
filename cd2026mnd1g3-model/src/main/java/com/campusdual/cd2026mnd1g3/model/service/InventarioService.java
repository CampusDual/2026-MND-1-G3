package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.MedicamentoDto;
import com.campusdual.cd2026mnd1g3.api.dto.LoteStockDto;
import com.campusdual.cd2026mnd1g3.api.dto.MovimientoInventarioDto;
import com.campusdual.cd2026mnd1g3.api.enums.TipoMovimiento;
import com.campusdual.cd2026mnd1g3.model.entity.LoteStockEntity;
import com.campusdual.cd2026mnd1g3.model.entity.MedicamentoEntity;
import com.campusdual.cd2026mnd1g3.model.entity.MovimientoInventarioEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.LoteStockRepository;
import com.campusdual.cd2026mnd1g3.model.repository.MedicamentoRepository;
import com.campusdual.cd2026mnd1g3.model.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional("ganaderiaTransactionManager")
public class InventarioService {
    private final MedicamentoRepository medicamentoRepository;
    private final LoteStockRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AlertaSistemaListener alertaListener;

    public InventarioService(MedicamentoRepository medicamentoRepository,
                             LoteStockRepository loteRepository,
                             MovimientoInventarioRepository movimientoRepository,
                             AlertaSistemaListener alertaListener) {
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.alertaListener = alertaListener;
    }

    @Transactional(transactionManager = "ganaderiaTransactionManager", readOnly = true)
    public List<MedicamentoDto> findMedicamentos(Long clinicaId) {
        return medicamentoRepository.findByClinicaId(clinicaId).stream().map(this::toMedicamentoDto).toList();
    }

    @Transactional(transactionManager = "ganaderiaTransactionManager", readOnly = true)
    public List<LoteStockDto> findLotes(Long clinicaId) {
        return loteRepository.findAll().stream()
                .filter(lote -> lote.getMedicamento().getClinicaId().equals(clinicaId))
                .map(lote -> new LoteStockDto(lote.getId(), lote.getMedicamento().getId(),
                        lote.getNumeroLote(), lote.getFechaCaducidad(), lote.getProveedor()))
                .toList();
    }

    @Transactional(transactionManager = "ganaderiaTransactionManager", readOnly = true)
    public List<MovimientoInventarioDto> findMovimientos(Long clinicaId) {
        return movimientoRepository.findByClinicaIdOrderByFechaHoraDesc(clinicaId).stream()
                .map(this::toMovimientoDto).toList();
    }

    public MovimientoInventarioDto registrarSalida(Long clinicaId, Long loteStockId, double cantidad,
                                                   TipoMovimiento tipo, Long veterinarioId,
                                                   Long animalId, Long explotacionId, Long protocoloEventoId) {
        if (loteStockId == null || cantidad <= 0) {
            throw new IllegalArgumentException("El lote y una cantidad positiva son obligatorios");
        }
        LoteStockEntity lote = loteRepository.findById(loteStockId)
                .orElseThrow(() -> new ResourceNotFoundException("LoteStock", loteStockId));
        if (!lote.getMedicamento().getClinicaId().equals(clinicaId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado");
        }
        MovimientoInventarioEntity movimiento = new MovimientoInventarioEntity();
        movimiento.setClinicaId(clinicaId);
        movimiento.setLoteStock(lote);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(-cantidad);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setVeterinarioId(veterinarioId);
        movimiento.setAnimalId(animalId);
        movimiento.setExplotacionId(explotacionId);
        movimiento.setProtocoloEventoId(protocoloEventoId);
        MovimientoInventarioEntity saved = movimientoRepository.save(movimiento);

        double stockMedicamento = movimientoRepository.sumByMedicamentoId(lote.getMedicamento().getId());
        if (stockMedicamento < lote.getMedicamento().getStockMinimoAlerta()) {
            alertaListener.createStockAlert(clinicaId, explotacionId, animalId,
                    "El medicamento " + lote.getMedicamento().getNombreComercial()
                            + " ha quedado por debajo del minimo configurado");
        }
        return toMovimientoDto(saved);
    }

    private MedicamentoDto toMedicamentoDto(MedicamentoEntity m) {
        return new MedicamentoDto(m.getId(), m.getClinicaId(), m.getNombreComercial(),
                m.getPrincipioActivo(), m.getEspecieDestino(), m.getCategoriaPran(),
                m.getDiasRetiradaDefecto(), m.getStockMinimoAlerta());
    }

    private MovimientoInventarioDto toMovimientoDto(MovimientoInventarioEntity m) {
        return new MovimientoInventarioDto(m.getId(), m.getClinicaId(), m.getLoteStock().getId(),
                m.getTipoMovimiento(), m.getCantidad(), m.getFechaHora(), m.getVeterinarioId(),
                m.getAnimalId(), m.getExplotacionId(), m.getProtocoloEventoId());
    }
}
