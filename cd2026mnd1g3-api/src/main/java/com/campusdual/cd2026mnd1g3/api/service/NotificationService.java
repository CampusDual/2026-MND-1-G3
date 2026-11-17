package com.campusdual.cd2026mnd1g3.api.service;

import com.campusdual.cd2026mnd1g3.api.dto.NotificationMessage;

/**
 * Puerto de salida para enviar notificaciones mediante cualquier proveedor.
 */
public interface NotificationService {

    /**
     * Envía un mensaje a un proveedor configurado.
     *
     * @param message mensaje normalizado
     */
    void send(NotificationMessage message);
}
