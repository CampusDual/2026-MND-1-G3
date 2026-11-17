package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.ProtocolEventDto;
import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import com.campusdual.cd2026mnd1g3.api.service.NotificationChannel;
import com.campusdual.cd2026mnd1g3.model.entity.*;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProtocolEventService {
    private final ProtocolEventRepository eventRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final NotificationChannel notificationChannel;

    public ProtocolEventService(ProtocolEventRepository eventRepository, FarmRepository farmRepository,
                                AnimalRepository animalRepository, NotificationChannel notificationChannel) {
        this.eventRepository = eventRepository;
        this.farmRepository = farmRepository;
        this.animalRepository = animalRepository;
        this.notificationChannel = notificationChannel;
    }

    @Transactional(readOnly = true)
    public List<ProtocolEventDto> findAll() {
        return eventRepository.findAll().stream().map(this::toDto).toList();
    }

    public ProtocolEventDto create(ProtocolEventDto dto) {
        validate(dto);
        FarmEntity farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Explotacion", dto.getFarmId()));
        AnimalEntity animal = dto.getAnimalId() == null ? null : animalRepository.findById(dto.getAnimalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal", dto.getAnimalId()));
        ProtocolEventEntity event = new ProtocolEventEntity();
        event.setEventType(dto.getEventType()); event.setTitle(dto.getTitle().trim()); event.setMedication(dto.getMedication());
        event.setApplicationDate(dto.getApplicationDate()); event.setWithdrawalDays(dto.getWithdrawalDays());
        event.setWithdrawalEndDate(dto.getWithdrawalDays() > 0 ? dto.getApplicationDate().plusDays(dto.getWithdrawalDays()) : null);
        event.setNextReminderDays(dto.getNextReminderDays()); event.setStatus(dto.getStatus() == null ? EventStatus.APLICADO : dto.getStatus());
        event.setAnimal(animal); event.setFarm(farm);
        ProtocolEventEntity saved = eventRepository.save(event);
        if (saved.getWithdrawalDays() > 0) {
            createNotification(saved, "Retirada sanitaria activa para " + saved.getTitle()
                    + ". Liberacion prevista: " + saved.getWithdrawalEndDate());
        }
        if (saved.getNextReminderDays() != null && saved.getNextReminderDays() > 0) {
            ProtocolEventEntity reminder = copyAsReminder(saved);
            ProtocolEventEntity reminderSaved = eventRepository.save(reminder);
            createNotification(reminderSaved, "Recordatorio programado: " + reminderSaved.getTitle());
        }
        return toDto(saved);
    }

    private ProtocolEventEntity copyAsReminder(ProtocolEventEntity source) {
        ProtocolEventEntity reminder = new ProtocolEventEntity();
        reminder.setEventType(source.getEventType()); reminder.setTitle(source.getTitle() + " (recordatorio)");
        reminder.setMedication(source.getMedication());
        reminder.setApplicationDate(source.getApplicationDate().plusDays(source.getNextReminderDays()));
        reminder.setWithdrawalDays(source.getWithdrawalDays()); reminder.setWithdrawalEndDate(null);
        reminder.setNextReminderDays(null); reminder.setStatus(EventStatus.PROGRAMADO);
        reminder.setAnimal(source.getAnimal()); reminder.setFarm(source.getFarm());
        return reminder;
    }

    private void createNotification(ProtocolEventEntity event, String message) {
        notificationChannel.scheduleOrSendMessage(
                event.getFarm().getWhatsappPhone(),
                message,
                java.time.LocalDateTime.now(),
                event.getId());
    }

    private void validate(ProtocolEventDto dto) {
        if (dto == null || dto.getEventType() == null || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getApplicationDate() == null || dto.getWithdrawalDays() == null || dto.getWithdrawalDays() < 0
                || dto.getFarmId() == null) {
            throw new IllegalArgumentException("Los datos del protocolo son invalidos");
        }
        if (dto.getApplicationDate().isBefore(LocalDate.now().minusYears(1))) {
            throw new IllegalArgumentException("La fecha de aplicacion no puede ser anterior a un ano");
        }
    }

    private ProtocolEventDto toDto(ProtocolEventEntity e) {
        return new ProtocolEventDto(e.getId(), e.getEventType(), e.getTitle(), e.getMedication(),
                e.getApplicationDate(), e.getWithdrawalDays(), e.getWithdrawalEndDate(), e.getNextReminderDays(),
                e.getStatus(), e.getAnimal() == null ? null : e.getAnimal().getId(), e.getFarm().getId());
    }
}
