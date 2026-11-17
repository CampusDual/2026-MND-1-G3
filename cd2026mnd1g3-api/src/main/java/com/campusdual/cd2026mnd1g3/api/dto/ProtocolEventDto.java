package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import com.campusdual.cd2026mnd1g3.api.enums.EventType;

import java.time.LocalDate;

public class ProtocolEventDto {
    private Long id;
    private EventType eventType;
    private String title;
    private String medication;
    private LocalDate applicationDate;
    private Integer withdrawalDays;
    private LocalDate withdrawalEndDate;
    private Integer nextReminderDays;
    private EventStatus status;
    private Long animalId;
    private Long farmId;
    private String medicationBatch;
    private Integer nextCycleDays;

    public ProtocolEventDto() {
    }

    public ProtocolEventDto(Long id, EventType eventType, String title, String medication, LocalDate applicationDate,
                            Integer withdrawalDays, LocalDate withdrawalEndDate, Integer nextReminderDays,
                            EventStatus status, Long animalId, Long farmId) {
        this.id = id; this.eventType = eventType; this.title = title; this.medication = medication;
        this.applicationDate = applicationDate; this.withdrawalDays = withdrawalDays;
        this.withdrawalEndDate = withdrawalEndDate; this.nextReminderDays = nextReminderDays;
        this.status = status; this.animalId = animalId; this.farmId = farmId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    public Integer getWithdrawalDays() { return withdrawalDays; }
    public void setWithdrawalDays(Integer withdrawalDays) { this.withdrawalDays = withdrawalDays; }
    public LocalDate getWithdrawalEndDate() { return withdrawalEndDate; }
    public void setWithdrawalEndDate(LocalDate withdrawalEndDate) { this.withdrawalEndDate = withdrawalEndDate; }
    public Integer getNextReminderDays() { return nextReminderDays; }
    public void setNextReminderDays(Integer nextReminderDays) { this.nextReminderDays = nextReminderDays; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    public String getMedicationBatch() { return medicationBatch; }
    public void setMedicationBatch(String medicationBatch) { this.medicationBatch = medicationBatch; }
    public Integer getNextCycleDays() { return nextCycleDays; }
    public void setNextCycleDays(Integer nextCycleDays) { this.nextCycleDays = nextCycleDays; }
}
