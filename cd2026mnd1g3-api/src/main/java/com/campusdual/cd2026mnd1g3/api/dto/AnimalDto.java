package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.HealthStatus;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.enums.Species;

import java.time.LocalDate;

public class AnimalDto {
    private Long id;
    private String earTag;
    private String name;
    private Species species;
    private LocalDate birthDate;
    private HealthStatus healthStatus;
    private Long farmId;
    private EstadoSanitario estadoSanitario;
    private LocalDate fechaFinRetirada;
    private String notas;

    public AnimalDto() {
    }

    public AnimalDto(Long id, String earTag, String name, Species species, LocalDate birthDate, HealthStatus healthStatus, Long farmId) {
        this.id = id; this.earTag = earTag; this.name = name; this.species = species;
        this.birthDate = birthDate; this.healthStatus = healthStatus; this.farmId = farmId;
    }

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
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    public EstadoSanitario getEstadoSanitario() { return estadoSanitario; }
    public void setEstadoSanitario(EstadoSanitario estadoSanitario) { this.estadoSanitario = estadoSanitario; }
    public LocalDate getFechaFinRetirada() { return fechaFinRetirada; }
    public void setFechaFinRetirada(LocalDate fechaFinRetirada) { this.fechaFinRetirada = fechaFinRetirada; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
