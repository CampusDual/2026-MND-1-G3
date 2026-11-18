package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.Species;

public class FarmDto {
    private Long id;
    private String name;
    private String regaCode;
    private Species mainSpecies;
    private String managerName;
    private Long clinicaId;
    private String veterinarianAssignedPhone;

    public FarmDto() {
    }

    public FarmDto(Long id, String name, String regaCode, Species mainSpecies, String managerName, Long clinicaId) {
        this.id = id;
        this.name = name;
        this.regaCode = regaCode;
        this.mainSpecies = mainSpecies;
        this.managerName = managerName;
        this.clinicaId = clinicaId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegaCode() { return regaCode; }
    public void setRegaCode(String regaCode) { this.regaCode = regaCode; }
    public Species getMainSpecies() { return mainSpecies; }
    public void setMainSpecies(Species mainSpecies) { this.mainSpecies = mainSpecies; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public String getVeterinarianAssignedPhone() { return veterinarianAssignedPhone; }
    public void setVeterinarianAssignedPhone(String veterinarianAssignedPhone) {
        this.veterinarianAssignedPhone = veterinarianAssignedPhone;
    }
}
