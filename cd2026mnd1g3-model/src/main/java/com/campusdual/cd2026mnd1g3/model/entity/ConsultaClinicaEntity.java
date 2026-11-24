package com.campusdual.cd2026mnd1g3.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas_clinicas")
public class ConsultaClinicaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PacienteClinicaEntity paciente;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private PropietarioClinicaEntity propietario;
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;
    @Column(length = 500)
    private String motivo;
    @Column(length = 2000)
    private String notas;
    @Column(name = "proxima_vacuna")
    private LocalDate proximaVacuna;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public PacienteClinicaEntity getPaciente() { return paciente; }
    public void setPaciente(PacienteClinicaEntity paciente) { this.paciente = paciente; }
    public PropietarioClinicaEntity getPropietario() { return propietario; }
    public void setPropietario(PropietarioClinicaEntity propietario) { this.propietario = propietario; }
    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDateTime fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public LocalDate getProximaVacuna() { return proximaVacuna; }
    public void setProximaVacuna(LocalDate proximaVacuna) { this.proximaVacuna = proximaVacuna; }
}
