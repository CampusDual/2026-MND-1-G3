package com.campusdual.cd2026mnd1g3.api.dto;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailDto {
    private PatientSummaryDto patient;
    private String vaccinationStatus;
    private java.time.LocalDate nextVaccinationDate;
    private List<PatientSummaryDto> ownerPets = new ArrayList<>();

    public PatientSummaryDto getPatient() { return patient; }
    public void setPatient(PatientSummaryDto patient) { this.patient = patient; }
    public String getVaccinationStatus() { return vaccinationStatus; }
    public void setVaccinationStatus(String vaccinationStatus) { this.vaccinationStatus = vaccinationStatus; }
    public java.time.LocalDate getNextVaccinationDate() { return nextVaccinationDate; }
    public void setNextVaccinationDate(java.time.LocalDate nextVaccinationDate) { this.nextVaccinationDate = nextVaccinationDate; }
    public List<PatientSummaryDto> getOwnerPets() { return ownerPets; }
    public void setOwnerPets(List<PatientSummaryDto> ownerPets) { this.ownerPets = ownerPets; }
}
