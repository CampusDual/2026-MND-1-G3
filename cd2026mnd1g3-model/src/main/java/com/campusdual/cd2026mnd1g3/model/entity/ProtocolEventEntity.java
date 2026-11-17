package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import com.campusdual.cd2026mnd1g3.api.enums.EventType;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "protocol_events")
public class ProtocolEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(length = 120)
    private String medication;
    @Column(name = "medication_batch", length = 120)
    private String medicationBatch;
    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;
    @Column(name = "withdrawal_days", nullable = false)
    private Integer withdrawalDays;
    @Column(name = "withdrawal_end_date")
    private LocalDate withdrawalEndDate;
    @Column(name = "next_reminder_days")
    private Integer nextReminderDays;
    @Column(name = "next_cycle_days", nullable = false)
    private Integer nextCycleDays = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private AnimalEntity animal;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private FarmEntity farm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }
    public String getMedicationBatch() { return medicationBatch; }
    public void setMedicationBatch(String medicationBatch) { this.medicationBatch = medicationBatch; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    public Integer getWithdrawalDays() { return withdrawalDays; }
    public void setWithdrawalDays(Integer withdrawalDays) { this.withdrawalDays = withdrawalDays; }
    public LocalDate getWithdrawalEndDate() { return withdrawalEndDate; }
    public void setWithdrawalEndDate(LocalDate withdrawalEndDate) { this.withdrawalEndDate = withdrawalEndDate; }
    public Integer getNextReminderDays() { return nextReminderDays; }
    public void setNextReminderDays(Integer nextReminderDays) { this.nextReminderDays = nextReminderDays; }
    public Integer getNextCycleDays() { return nextCycleDays; }
    public void setNextCycleDays(Integer nextCycleDays) { this.nextCycleDays = nextCycleDays; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public AnimalEntity getAnimal() { return animal; }
    public void setAnimal(AnimalEntity animal) { this.animal = animal; }
    public FarmEntity getFarm() { return farm; }
    public void setFarm(FarmEntity farm) { this.farm = farm; }
}
