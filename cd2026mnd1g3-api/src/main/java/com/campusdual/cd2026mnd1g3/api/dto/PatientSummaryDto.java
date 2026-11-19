package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.Species;
import java.time.LocalDate;

public class PatientSummaryDto {
    private Long id;
    private String name;
    private Species species;
    private String breed;
    private String microchip;
    private LocalDate birthDate;
    private String estadoSanitario;
    private PatientOwnerDto owner;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getMicrochip() { return microchip; }
    public void setMicrochip(String microchip) { this.microchip = microchip; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getEstadoSanitario() { return estadoSanitario; }
    public void setEstadoSanitario(String estadoSanitario) { this.estadoSanitario = estadoSanitario; }
    public PatientOwnerDto getOwner() { return owner; }
    public void setOwner(PatientOwnerDto owner) { this.owner = owner; }
}
