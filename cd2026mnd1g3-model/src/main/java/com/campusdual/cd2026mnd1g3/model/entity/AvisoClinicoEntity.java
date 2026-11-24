package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.CategoriaAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.MotivoDescarteAviso;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "avisos_clinicos")
public class AvisoClinicoEntity {
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaAvisoClinico categoria;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(name = "detalle_clinico", length = 2000)
    private String detalleClinico;
    @Column(name = "fecha_objetivo", nullable = false)
    private LocalDate fechaObjetivo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAvisoClinico estado = EstadoAvisoClinico.PENDIENTE;
    @Column(nullable = false)
    private int intentos;
    @Column(name = "ultimo_intento")
    private LocalDateTime ultimoIntento;
    @Column(name = "ultimo_usuario", length = 80)
    private String ultimoUsuario;
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Column(length = 30)
    private String progreso;
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_descarte", length = 30)
    private MotivoDescarteAviso motivoDescarte;
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    @Column(name = "cancelado_por_baja", nullable = false)
    private boolean canceladoPorBaja;
    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public PacienteClinicaEntity getPaciente() { return paciente; }
    public void setPaciente(PacienteClinicaEntity paciente) { this.paciente = paciente; }
    public PropietarioClinicaEntity getPropietario() { return propietario; }
    public void setPropietario(PropietarioClinicaEntity propietario) { this.propietario = propietario; }
    public CategoriaAvisoClinico getCategoria() { return categoria; }
    public void setCategoria(CategoriaAvisoClinico categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDetalleClinico() { return detalleClinico; }
    public void setDetalleClinico(String detalleClinico) { this.detalleClinico = detalleClinico; }
    public LocalDate getFechaObjetivo() { return fechaObjetivo; }
    public void setFechaObjetivo(LocalDate fechaObjetivo) { this.fechaObjetivo = fechaObjetivo; }
    public EstadoAvisoClinico getEstado() { return estado; }
    public void setEstado(EstadoAvisoClinico estado) { this.estado = estado; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
    public LocalDateTime getUltimoIntento() { return ultimoIntento; }
    public void setUltimoIntento(LocalDateTime ultimoIntento) { this.ultimoIntento = ultimoIntento; }
    public String getUltimoUsuario() { return ultimoUsuario; }
    public void setUltimoUsuario(String ultimoUsuario) { this.ultimoUsuario = ultimoUsuario; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getProgreso() { return progreso; }
    public void setProgreso(String progreso) { this.progreso = progreso; }
    public MotivoDescarteAviso getMotivoDescarte() { return motivoDescarte; }
    public void setMotivoDescarte(MotivoDescarteAviso motivoDescarte) { this.motivoDescarte = motivoDescarte; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public boolean isCanceladoPorBaja() { return canceladoPorBaja; }
    public void setCanceladoPorBaja(boolean canceladoPorBaja) { this.canceladoPorBaja = canceladoPorBaja; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
