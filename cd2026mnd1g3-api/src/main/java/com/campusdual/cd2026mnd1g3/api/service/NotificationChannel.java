package com.campusdual.cd2026mnd1g3.api.service;

import java.time.LocalDateTime;

/**
 * Canal de salida para programar o enviar notificaciones.
 */
public interface NotificationChannel {

    /**
     * Programa o envía un mensaje asociado a un evento de protocolo.
     *
     * @param phone teléfono destinatario
     * @param message contenido del mensaje
     * @param scheduledFor fecha y hora programadas
     * @param eventId identificador del evento asociado
     */
    void scheduleOrSendMessage(String phone, String message, LocalDateTime scheduledFor, Long eventId);
}
