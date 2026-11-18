package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.ProtocolEventDto;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import com.campusdual.cd2026mnd1g3.model.entity.ProtocolEventEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import com.campusdual.cd2026mnd1g3.model.repository.ProtocolEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.ApplicationEventPublisher;
import com.campusdual.cd2026mnd1g3.model.event.EstadoSanitarioCambiadoEvent;
import com.campusdual.cd2026mnd1g3.model.event.ProtocoloRegistradoEvent;
import com.campusdual.cd2026mnd1g3.api.enums.TipoMovimiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Motor de protocolos sanitarios cíclicos y sus avisos ganaderos.
 */
@Service
@Transactional
public class ProtocoloCiclicoService {

    private final ProtocolEventRepository eventRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final InventarioService inventarioService;

    public ProtocoloCiclicoService(ProtocolEventRepository eventRepository,
                                   FarmRepository farmRepository,
                                   AnimalRepository animalRepository,
                                   ApplicationEventPublisher eventPublisher,
                                   InventarioService inventarioService) {
        this.eventRepository = eventRepository;
        this.farmRepository = farmRepository;
        this.animalRepository = animalRepository;
        this.eventPublisher = eventPublisher;
        this.inventarioService = inventarioService;
    }

    public ProtocolEventDto registrarProtocolo(ProtocolEventDto dto) {
        validate(dto);
        FarmEntity farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Explotacion", dto.getFarmId()));
        AnimalEntity animal = dto.getAnimalId() == null ? null : animalRepository.findById(dto.getAnimalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal", dto.getAnimalId()));

        ProtocolEventEntity event = new ProtocolEventEntity();
        event.setEventType(dto.getEventType());
        event.setTitle(dto.getTitle().trim());
        event.setApplicationDate(dto.getApplicationDate());
        event.setWithdrawalDays(dto.getWithdrawalDays());
        event.setWithdrawalEndDate(dto.getWithdrawalDays() > 0
                ? dto.getApplicationDate().plusDays(dto.getWithdrawalDays()) : null);
        event.setNextCycleDays(dto.getNextCycleDays() == null ? 0 : dto.getNextCycleDays());
        event.setNextReminderDays(dto.getNextReminderDays());
        event.setStatus(dto.getStatus() == null ? EventStatus.PROGRAMADO : dto.getStatus());
        event.setAnimal(animal);
        event.setFarm(farm);
        ProtocolEventEntity saved = eventRepository.save(event);
        Long clinicaId = farm.getClinicaId();
        if (dto.getLoteStockId() != null && dto.getCantidadUsada() != null
                && dto.getCantidadUsada() > 0) {
            inventarioService.registrarSalida(clinicaId, dto.getLoteStockId(), dto.getCantidadUsada(),
                    TipoMovimiento.SALIDA_GANADERIA, null, animal == null ? null : animal.getId(),
                    farm.getId(), saved.getId());
        }
        eventPublisher.publishEvent(new ProtocoloRegistradoEvent(saved.getId(), clinicaId,
                farm.getId(), animal == null ? null : animal.getId(), saved.getTitle(),
                saved.getWithdrawalDays() > 0,
                saved.getApplicationDate().atStartOfDay()));

        if (saved.getWithdrawalDays() > 0 && animal != null) {
            animal.setFechaFinRetirada(saved.getWithdrawalEndDate());
            animal.setEstadoSanitario(EstadoSanitario.EN_RETIRADA_MEDICAMENTO);
            animalRepository.save(animal);
        }
        if (saved.getNextCycleDays() > 0) {
            ProtocolEventEntity next = createNextCycle(saved);
            eventRepository.save(next);
        }
        return toDto(saved);
    }

    public void actualizarEstadoSanitario(Long animalId, EstadoSanitario nuevoEstado, String notas) {
        if (animalId == null || animalId <= 0 || nuevoEstado == null) {
            throw new IllegalArgumentException("El animal y el estado sanitario son obligatorios");
        }
        AnimalEntity animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal", animalId));
        animal.setEstadoSanitario(nuevoEstado);
        animal.setNotas(notas);
        animalRepository.save(animal);
        eventPublisher.publishEvent(new EstadoSanitarioCambiadoEvent(
                animal.getFarm().getClinicaId(), animal.getFarm().getId(), animal.getId(),
                nuevoEstado, notas));
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void ejecutarCicloDiario() {
        ejecutarRevisionDiaria();
    }

    public void ejecutarRevisionDiaria() {
        LocalDate today = LocalDate.now();
        eventRepository.findByApplicationDateAndStatus(today, EventStatus.PROGRAMADO)
                .forEach(event -> eventPublisher.publishEvent(new ProtocoloRegistradoEvent(
                        event.getId(), event.getFarm().getClinicaId(), event.getFarm().getId(),
                        event.getAnimal() == null ? null : event.getAnimal().getId(),
                        event.getTitle(), event.getWithdrawalDays() > 0, LocalDateTime.now())));
        animalRepository.findByFechaFinRetiradaLessThanEqualAndEstadoSanitario(
                        today, EstadoSanitario.EN_RETIRADA_MEDICAMENTO).forEach(animal -> {
            animal.setEstadoSanitario(EstadoSanitario.SANO);
            animal.setFechaFinRetirada(null);
            animalRepository.save(animal);
            eventPublisher.publishEvent(new EstadoSanitarioCambiadoEvent(
                    animal.getFarm().getClinicaId(), animal.getFarm().getId(), animal.getId(),
                    EstadoSanitario.SANO, "Finaliza la retirada sanitaria"));
        });
    }

    private ProtocolEventEntity createNextCycle(ProtocolEventEntity source) {
        ProtocolEventEntity next = new ProtocolEventEntity();
        next.setEventType(source.getEventType());
        next.setTitle(source.getTitle() + " (siguiente ciclo)");
        next.setApplicationDate(source.getApplicationDate().plusDays(source.getNextCycleDays()));
        next.setWithdrawalDays(source.getWithdrawalDays());
        next.setNextCycleDays(source.getNextCycleDays());
        next.setStatus(EventStatus.PROGRAMADO);
        next.setAnimal(source.getAnimal());
        next.setFarm(source.getFarm());
        return next;
    }

    private Long findEventIdForAlert(AnimalEntity animal) {
        return eventRepository.findByFarmIdOrderByApplicationDateAsc(animal.getFarm().getId()).stream()
                .filter(event -> event.getAnimal() != null && event.getAnimal().getId().equals(animal.getId()))
                .map(ProtocolEventEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El animal no tiene eventos de protocolo"));
    }

    private void validate(ProtocolEventDto dto) {
        if (dto == null || dto.getEventType() == null || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getApplicationDate() == null || dto.getWithdrawalDays() == null
                || dto.getWithdrawalDays() < 0 || dto.getFarmId() == null
                || (dto.getNextCycleDays() != null && dto.getNextCycleDays() < 0)) {
            throw new IllegalArgumentException("Los datos del protocolo son invalidos");
        }
    }

    private ProtocolEventDto toDto(ProtocolEventEntity event) {
        ProtocolEventDto dto = new ProtocolEventDto(event.getId(), event.getEventType(), event.getTitle(),
                event.getApplicationDate(), event.getWithdrawalDays(),
                event.getWithdrawalEndDate(), event.getNextReminderDays(), event.getStatus(),
                event.getAnimal() == null ? null : event.getAnimal().getId(), event.getFarm().getId());
        dto.setNextCycleDays(event.getNextCycleDays());
        return dto;
    }
}
