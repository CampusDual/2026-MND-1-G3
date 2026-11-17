package com.campusdual.cd2026mnd1g3;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;
import com.campusdual.cd2026mnd1g3.api.enums.EventType;
import com.campusdual.cd2026mnd1g3.api.enums.HealthStatus;
import com.campusdual.cd2026mnd1g3.api.enums.NotificationStatus;
import com.campusdual.cd2026mnd1g3.api.enums.Species;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import com.campusdual.cd2026mnd1g3.model.entity.ProtocolEventEntity;
import com.campusdual.cd2026mnd1g3.model.entity.WhatsAppNotificationEntity;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import com.campusdual.cd2026mnd1g3.model.repository.ProtocolEventRepository;
import com.campusdual.cd2026mnd1g3.model.repository.WhatsAppNotificationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carga datos de demostración únicamente sobre una base de datos ganadera vacía.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final ProtocolEventRepository eventRepository;
    private final WhatsAppNotificationRepository notificationRepository;

    public DataInitializer(FarmRepository farmRepository, AnimalRepository animalRepository,
                           ProtocolEventRepository eventRepository,
                           WhatsAppNotificationRepository notificationRepository) {
        this.farmRepository = farmRepository;
        this.animalRepository = animalRepository;
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (farmRepository.count() > 0 || animalRepository.count() > 0
                || eventRepository.count() > 0 || notificationRepository.count() > 0) {
            return;
        }

        FarmEntity dairyFarm = createFarm("Ganadería El Roble", "ES2800000001",
                Species.VACUNO_LECHE, "Laura García", "+34610000001", "+34610000011");
        FarmEntity pigFarm = createFarm("Granja Los Pinos", "ES2800000002",
                Species.PORCINO, "Miguel López", "+34610000002", "+34610000022");

        AnimalEntity healthy = createAnimal("ES2800000001-001", "Bruma", Species.VACUNO_LECHE,
                HealthStatus.HEALTHY, EstadoSanitario.SANO, dairyFarm, null);
        AnimalEntity withdrawal = createAnimal("ES2800000001-002", "Niebla", Species.VACUNO_LECHE,
                HealthStatus.SICK, EstadoSanitario.EN_RETIRADA_MEDICAMENTO, dairyFarm,
                LocalDate.now().plusDays(5));
        AnimalEntity sick = createAnimal("ES2800000002-001", "Pino", Species.PORCINO,
                HealthStatus.SICK, EstadoSanitario.ENFERMO, pigFarm, null);
        createAnimal("ES2800000002-002", "Roble", Species.PORCINO,
                HealthStatus.QUARANTINE, EstadoSanitario.CUARENTENA, pigFarm, null);

        ProtocolEventEntity vaccine = createEvent(EventType.VACUNACION, "Revacunación anual",
                null, LocalDate.now(), 0, 30, EventStatus.APLICADO, healthy, dairyFarm);
        ProtocolEventEntity treatment = createEvent(EventType.TRATAMIENTO_MEDICO, "Tratamiento antibiótico",
                "Amoxicilina", LocalDate.now().minusDays(1), 5, 0, EventStatus.APLICADO,
                withdrawal, dairyFarm);
        ProtocolEventEntity checkup = createEvent(EventType.REVISION_PERIODICA, "Revisión veterinaria",
                null, LocalDate.now().plusDays(1), 0, 0, EventStatus.PROGRAMADO, sick, pigFarm);

        createNotification(dairyFarm.getWhatsappPhone(), "Próxima revacunación de Bruma", vaccine,
                NotificationStatus.PENDIENTE);
        createNotification(dairyFarm.getWhatsappPhone(), "Retirada activa de Niebla", treatment,
                NotificationStatus.ENVIADO);
        createNotification(pigFarm.getWhatsappPhone(), "Revisión veterinaria de Pino", checkup,
                NotificationStatus.CONFIRMADO_GANADERO);
    }

    private FarmEntity createFarm(String name, String regaCode, Species species, String manager,
                                  String whatsapp, String veterinarianPhone) {
        FarmEntity farm = new FarmEntity();
        farm.setName(name);
        farm.setRegaCode(regaCode);
        farm.setMainSpecies(species);
        farm.setManagerName(manager);
        farm.setWhatsappPhone(whatsapp);
        farm.setVeterinarianAssignedPhone(veterinarianPhone);
        return farmRepository.save(farm);
    }

    private AnimalEntity createAnimal(String earTag, String name, Species species,
                                      HealthStatus healthStatus, EstadoSanitario sanitaryStatus,
                                      FarmEntity farm, LocalDate withdrawalEnd) {
        AnimalEntity animal = new AnimalEntity();
        animal.setEarTag(earTag);
        animal.setName(name);
        animal.setSpecies(species);
        animal.setBirthDate(LocalDate.of(2022, 1, 15));
        animal.setHealthStatus(healthStatus);
        animal.setEstadoSanitario(sanitaryStatus);
        animal.setFechaFinRetirada(withdrawalEnd);
        animal.setFarm(farm);
        return animalRepository.save(animal);
    }

    private ProtocolEventEntity createEvent(EventType type, String title, String medication,
                                            LocalDate applicationDate, int withdrawalDays,
                                            int nextCycleDays, EventStatus status,
                                            AnimalEntity animal, FarmEntity farm) {
        ProtocolEventEntity event = new ProtocolEventEntity();
        event.setEventType(type);
        event.setTitle(title);
        event.setMedication(medication);
        event.setApplicationDate(applicationDate);
        event.setWithdrawalDays(withdrawalDays);
        event.setWithdrawalEndDate(withdrawalDays > 0
                ? applicationDate.plusDays(withdrawalDays) : null);
        event.setNextCycleDays(nextCycleDays);
        event.setStatus(status);
        event.setAnimal(animal);
        event.setFarm(farm);
        return eventRepository.save(event);
    }

    private void createNotification(String phone, String message, ProtocolEventEntity event,
                                    NotificationStatus status) {
        WhatsAppNotificationEntity notification = new WhatsAppNotificationEntity();
        notification.setDestinationPhone(phone);
        notification.setMessage(message);
        notification.setScheduledAt(LocalDateTime.now());
        notification.setSentAt(status == NotificationStatus.PENDIENTE ? null : LocalDateTime.now());
        notification.setSendStatus(status);
        notification.setProtocolEvent(event);
        notificationRepository.save(notification);
    }
}
