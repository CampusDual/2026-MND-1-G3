package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.Species;
import jakarta.persistence.*;

@Entity
@Table(name = "farms")
public class FarmEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "rega_code", nullable = false, unique = true, length = 30)
    private String regaCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "main_species", nullable = false, length = 30)
    private Species mainSpecies;
    @Column(name = "manager_name", nullable = false, length = 120)
    private String managerName;
    @Column(name = "veterinarian_assigned_phone", length = 30)
    private String veterinarianAssignedPhone;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId = 1L;

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
    public String getVeterinarianAssignedPhone() { return veterinarianAssignedPhone; }
    public void setVeterinarianAssignedPhone(String veterinarianAssignedPhone) {
        this.veterinarianAssignedPhone = veterinarianAssignedPhone;
    }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
}
