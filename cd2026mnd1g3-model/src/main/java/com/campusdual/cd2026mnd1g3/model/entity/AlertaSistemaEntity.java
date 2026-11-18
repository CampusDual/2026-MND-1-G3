package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.DestinatarioRol;
import com.campusdual.cd2026mnd1g3.api.enums.TipoAlerta;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_sistema")
public class AlertaSistemaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @Enumerated(EnumType.STRING)
    @Column(name = "destinatario_rol", nullable = false, length = 30)
    private DestinatarioRol destinatarioRol;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 40)
    private TipoAlerta tipoAlerta;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(nullable = false, length = 1000)
    private String mensaje;
    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;
    @Column(nullable = false)
    private boolean leida;
    @Column(name = "confirmada_por_granja", nullable = false)
    private boolean confirmadaPorGranja;
    @Column(name = "explotacion_id")
    private Long explotacionId;
    @Column(name = "animal_id")
    private Long animalId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public DestinatarioRol getDestinatarioRol() { return destinatarioRol; }
    public void setDestinatarioRol(DestinatarioRol destinatarioRol) { this.destinatarioRol = destinatarioRol; }
    public TipoAlerta getTipoAlerta() { return tipoAlerta; }
    public void setTipoAlerta(TipoAlerta tipoAlerta) { this.tipoAlerta = tipoAlerta; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public boolean isConfirmadaPorGranja() { return confirmadaPorGranja; }
    public void setConfirmadaPorGranja(boolean confirmadaPorGranja) { this.confirmadaPorGranja = confirmadaPorGranja; }
    public Long getExplotacionId() { return explotacionId; }
    public void setExplotacionId(Long explotacionId) { this.explotacionId = explotacionId; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
}
