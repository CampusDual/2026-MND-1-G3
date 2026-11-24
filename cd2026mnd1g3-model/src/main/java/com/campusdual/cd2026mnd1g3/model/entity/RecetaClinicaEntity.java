package com.campusdual.cd2026mnd1g3.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "recetas_clinicas")
public class RecetaClinicaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false)
    private ConsultaClinicaEntity consulta;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PacienteClinicaEntity paciente;
    @Column(name = "medicamento_id")
    private Long medicamentoId;
    @Column(name = "lote_stock_id")
    private Long loteStockId;
    @Column(length = 1000)
    private String indicaciones;
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public ConsultaClinicaEntity getConsulta() { return consulta; }
    public void setConsulta(ConsultaClinicaEntity consulta) { this.consulta = consulta; }
    public PacienteClinicaEntity getPaciente() { return paciente; }
    public void setPaciente(PacienteClinicaEntity paciente) { this.paciente = paciente; }
    public Long getMedicamentoId() { return medicamentoId; }
    public void setMedicamentoId(Long medicamentoId) { this.medicamentoId = medicamentoId; }
    public Long getLoteStockId() { return loteStockId; }
    public void setLoteStockId(Long loteStockId) { this.loteStockId = loteStockId; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
}
