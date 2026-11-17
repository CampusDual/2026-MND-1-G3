package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.Species;

public class FarmDto {
    private Long id;
    private String name;
    private String regaCode;
    private Species mainSpecies;
    private String managerName;
    private String whatsappPhone;
    private String veterinarianAssignedPhone;

    public FarmDto() {
    }

    public FarmDto(Long id, String name, String regaCode, Species mainSpecies, String managerName, String whatsappPhone) {
        this.id = id;
        this.name = name;
        this.regaCode = regaCode;
        this.mainSpecies = mainSpecies;
        this.managerName = managerName;
        this.whatsappPhone = whatsappPhone;
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
    public String getWhatsappPhone() { return whatsappPhone; }
    public void setWhatsappPhone(String whatsappPhone) { this.whatsappPhone = whatsappPhone; }
    public String getVeterinarianAssignedPhone() { return veterinarianAssignedPhone; }
    public void setVeterinarianAssignedPhone(String veterinarianAssignedPhone) {
        this.veterinarianAssignedPhone = veterinarianAssignedPhone;
    }
}
