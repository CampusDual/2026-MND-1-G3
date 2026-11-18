package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.TipoMovimiento;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "movimientos_inventario")
public class MovimientoInventarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_stock_id", nullable = false)
    private LoteStockEntity loteStock;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private TipoMovimiento tipoMovimiento;
    @Column(nullable = false)
    private double cantidad;
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    @Column(name = "veterinario_id")
    private Long veterinarioId;
    @Column(name = "animal_id")
    private Long animalId;
    @Column(name = "explotacion_id")
    private Long explotacionId;
    @Column(name = "protocolo_evento_id")
    private Long protocoloEventoId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public LoteStockEntity getLoteStock() { return loteStock; }
    public void setLoteStock(LoteStockEntity loteStock) { this.loteStock = loteStock; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public Long getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Long veterinarioId) { this.veterinarioId = veterinarioId; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
    public Long getExplotacionId() { return explotacionId; }
    public void setExplotacionId(Long explotacionId) { this.explotacionId = explotacionId; }
    public Long getProtocoloEventoId() { return protocoloEventoId; }
    public void setProtocoloEventoId(Long protocoloEventoId) { this.protocoloEventoId = protocoloEventoId; }
}
