package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.service.NotificationChannel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Fachada del simulador de WhatsApp para las reglas de dominio.
 */
@Service
public class WhatsAppSimulatorService {

    private final WhatsAppNotificationService notificationService;

    public WhatsAppSimulatorService(WhatsAppNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void scheduleOrSendMessage(String phone, String message, LocalDateTime scheduledFor, Long eventId) {
        notificationService.scheduleOrSendMessage(phone, message, scheduledFor, eventId);
    }

    public void sendImmediateAlert(String phone, String message, Long eventId) {
        notificationService.sendImmediately(phone, message, eventId);
    }
}
