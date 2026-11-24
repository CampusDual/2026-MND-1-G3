package com.campusdual.cd2026mnd1g3.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "propietarios")
public class PropietarioClinicaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, length = 120)
    private String apellidos;
    @Column(name = "dni_nif", length = 20)
    private String dniNif;
    @Column(nullable = false, length = 30)
    private String telefono;
    @Column(length = 160)
    private String email;
    @Column(name = "nombre_normalizado", nullable = false, length = 250)
    private String nombreNormalizado;
    @Column(name = "notificaciones_internas", nullable = false)
    private boolean notificacionesInternas = true;
    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDniNif() { return dniNif; }
    public void setDniNif(String dniNif) { this.dniNif = dniNif; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombreNormalizado() { return nombreNormalizado; }
    public void setNombreNormalizado(String nombreNormalizado) { this.nombreNormalizado = nombreNormalizado; }
    public boolean isNotificacionesInternas() { return notificacionesInternas; }
    public void setNotificacionesInternas(boolean notificacionesInternas) { this.notificacionesInternas = notificacionesInternas; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
