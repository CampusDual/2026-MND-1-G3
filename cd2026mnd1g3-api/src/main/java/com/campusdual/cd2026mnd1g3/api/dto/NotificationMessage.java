package com.campusdual.cd2026mnd1g3.api.dto;

/**
 * Mensaje normalizado que el núcleo entrega a un adaptador de notificaciones.
 *
 * @param recipient destinatario del mensaje
 * @param subject asunto opcional
 * @param content contenido del mensaje
 */
public record NotificationMessage(String recipient, String subject, String content) {
}
