package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.WhatsAppNotificationDto;
import com.campusdual.cd2026mnd1g3.model.service.WhatsAppNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {
    private final WhatsAppNotificationService service;
    public NotificationController(WhatsAppNotificationService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<List<WhatsAppNotificationDto>> findAll() { return ResponseEntity.ok(service.findAll()); }
    @PostMapping("/{id}/simular-envio")
    public ResponseEntity<WhatsAppNotificationDto> simulateSend(@PathVariable Long id) {
        return ResponseEntity.ok(service.simulateSend(id));
    }
    @PostMapping("/{id}/simular-respuesta-ganadero")
    public ResponseEntity<WhatsAppNotificationDto> simulateConfirmation(@PathVariable Long id) {
        return ResponseEntity.ok(service.simulateConfirmation(id));
    }

    @PostMapping("/{id}/simular-confirmacion-ganadero")
    public ResponseEntity<WhatsAppNotificationDto> simulateFarmerConfirmation(@PathVariable Long id) {
        return ResponseEntity.ok(service.simulateConfirmation(id));
    }
}
