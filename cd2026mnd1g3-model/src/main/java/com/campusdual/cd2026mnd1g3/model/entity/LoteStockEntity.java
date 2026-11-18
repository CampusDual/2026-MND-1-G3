package com.campusdual.cd2026mnd1g3.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "lotes_stock")
public class LoteStockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private MedicamentoEntity medicamento;
    @Column(name = "numero_lote", nullable = false, length = 80)
    private String numeroLote;
    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDate fechaCaducidad;
    @Column(nullable = false, length = 150)
    private String proveedor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MedicamentoEntity getMedicamento() { return medicamento; }
    public void setMedicamento(MedicamentoEntity medicamento) { this.medicamento = medicamento; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
}
