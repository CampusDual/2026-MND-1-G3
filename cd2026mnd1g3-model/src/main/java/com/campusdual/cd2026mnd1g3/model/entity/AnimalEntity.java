package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.HealthStatus;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.enums.Species;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "animals")
public class AnimalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ear_tag", nullable = false, unique = true, length = 40)
    private String earTag;
    @Column(length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Species species;
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false, length = 30)
    private HealthStatus healthStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sanitario", nullable = false, length = 40)
    private EstadoSanitario estadoSanitario = EstadoSanitario.SANO;
    @Column(name = "fecha_fin_retirada")
    private LocalDate fechaFinRetirada;
    @Column(length = 1000)
    private String notas;
    @Column(length = 40)
    private String subtipo;
    @Column(length = 80)
    private String ubicacion;
    @Column(name = "cantidad_cabezas", nullable = false)
    private Integer cantidadCabezas = 1;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId = 1L;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private FarmEntity farm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEarTag() { return earTag; }
    public void setEarTag(String earTag) { this.earTag = earTag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus healthStatus) { this.healthStatus = healthStatus; }
    public FarmEntity getFarm() { return farm; }
    public void setFarm(FarmEntity farm) { this.farm = farm; }
    public EstadoSanitario getEstadoSanitario() { return estadoSanitario; }
    public void setEstadoSanitario(EstadoSanitario estadoSanitario) { this.estadoSanitario = estadoSanitario; }
    public LocalDate getFechaFinRetirada() { return fechaFinRetirada; }
    public void setFechaFinRetirada(LocalDate fechaFinRetirada) { this.fechaFinRetirada = fechaFinRetirada; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getSubtipo() { return subtipo; }
    public void setSubtipo(String subtipo) { this.subtipo = subtipo; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Integer getCantidadCabezas() { return cantidadCabezas; }
    public void setCantidadCabezas(Integer cantidadCabezas) { this.cantidadCabezas = cantidadCabezas; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
}
