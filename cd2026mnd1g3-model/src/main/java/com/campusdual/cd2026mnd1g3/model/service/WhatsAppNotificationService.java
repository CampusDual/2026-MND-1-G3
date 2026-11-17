package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.WhatsAppNotificationDto;
import com.campusdual.cd2026mnd1g3.api.enums.NotificationStatus;
import com.campusdual.cd2026mnd1g3.api.service.NotificationChannel;
import com.campusdual.cd2026mnd1g3.model.entity.WhatsAppNotificationEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.ProtocolEventRepository;
import com.campusdual.cd2026mnd1g3.model.repository.WhatsAppNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WhatsAppNotificationService implements NotificationChannel {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhatsAppNotificationService.class);

    private final WhatsAppNotificationRepository repository;
    private final ProtocolEventRepository protocolEventRepository;

    public WhatsAppNotificationService(WhatsAppNotificationRepository repository,
                                       ProtocolEventRepository protocolEventRepository) {
        this.repository = repository;
        this.protocolEventRepository = protocolEventRepository;
    }

    @Override
    public void scheduleOrSendMessage(String phone, String message, LocalDateTime scheduledFor, Long eventId) {
        validateMessage(phone, message, scheduledFor, eventId);
        WhatsAppNotificationEntity entity = new WhatsAppNotificationEntity();
        entity.setDestinationPhone(phone.trim());
        entity.setMessage(message.trim());
        entity.setScheduledAt(scheduledFor);
        entity.setSendStatus(NotificationStatus.PENDIENTE);
        entity.setProtocolEvent(protocolEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento de protocolo", eventId)));
        repository.save(entity);
        LOGGER.info("[WHATSAPP SIMULADOR -> {}]: {}", phone, message);
    }

    @Transactional(readOnly = true)
    public List<WhatsAppNotificationDto> findAll() {
        return repository.findAllByOrderByScheduledAtAsc().stream().map(this::toDto).toList();
    }

    public WhatsAppNotificationDto simulateSend(Long id) {
        WhatsAppNotificationEntity entity = get(id);
        if (entity.getSendStatus() != NotificationStatus.PENDIENTE) {
            throw new IllegalArgumentException("La notificacion ya no esta pendiente");
        }
        entity.setSendStatus(NotificationStatus.ENVIADO);
        entity.setSentAt(LocalDateTime.now());
        LOGGER.info("[WHATSAPP SIMULADOR -> {}]: {}", entity.getDestinationPhone(), entity.getMessage());
        return toDto(repository.save(entity));
    }

    public WhatsAppNotificationDto simulateConfirmation(Long id) {
        WhatsAppNotificationEntity entity = get(id);
        if (entity.getSendStatus() != NotificationStatus.ENVIADO) {
            throw new IllegalArgumentException("La notificacion debe estar enviada antes de confirmarse");
        }
        entity.setSendStatus(NotificationStatus.CONFIRMADO_GANADERO);
        return toDto(repository.save(entity));
    }

    /**
     * Simula un envío inmediato creando la notificación y marcándola como enviada.
     *
     * @param phone teléfono destinatario
     * @param message contenido del mensaje
     * @param eventId evento asociado
     * @return notificación enviada
     */
    public WhatsAppNotificationDto sendImmediately(String phone, String message, Long eventId) {
        scheduleOrSendMessage(phone, message, LocalDateTime.now(), eventId);
        WhatsAppNotificationEntity entity = repository.findAllByOrderByScheduledAtAsc().stream()
                .filter(notification -> notification.getProtocolEvent().getId().equals(eventId)
                        && notification.getDestinationPhone().equals(phone.trim())
                        && notification.getMessage().equals(message.trim())
                        && notification.getSendStatus() == NotificationStatus.PENDIENTE)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No se pudo crear la notificacion"));
        return simulateSend(entity.getId());
    }

    private WhatsAppNotificationEntity get(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("El id debe ser mayor que cero");
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notificacion", id));
    }

    private void validateMessage(String phone, String message, LocalDateTime scheduledFor, Long eventId) {
        if (phone == null || phone.isBlank() || message == null || message.isBlank()
                || scheduledFor == null || eventId == null || eventId <= 0) {
            throw new IllegalArgumentException("Los datos de la notificacion son obligatorios");
        }
    }

    private WhatsAppNotificationDto toDto(WhatsAppNotificationEntity e) {
        return new WhatsAppNotificationDto(e.getId(), e.getDestinationPhone(), e.getMessage(),
                e.getScheduledAt(), e.getSentAt(), e.getSendStatus(), e.getProtocolEvent().getId());
    }
}
