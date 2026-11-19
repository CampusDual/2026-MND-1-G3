package com.campusdual.cd2026mnd1g3.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient_owners")
public class PatientOwnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;
    @Column(name = "normalized_full_name", length = 160)
    private String normalizedFullName;
    @Column(name = "phone_e164", length = 20)
    private String phoneE164;
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;
    @OneToMany(mappedBy = "owner")
    private List<AnimalEntity> animals = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getNormalizedFullName() { return normalizedFullName; }
    public void setNormalizedFullName(String normalizedFullName) { this.normalizedFullName = normalizedFullName; }
    public String getPhoneE164() { return phoneE164; }
    public void setPhoneE164(String phoneE164) { this.phoneE164 = phoneE164; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    public List<AnimalEntity> getAnimals() { return animals; }
    public void setAnimals(List<AnimalEntity> animals) { this.animals = animals; }
}
