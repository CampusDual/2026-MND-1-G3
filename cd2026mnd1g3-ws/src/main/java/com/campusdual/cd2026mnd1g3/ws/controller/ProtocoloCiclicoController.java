package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.dto.ProtocolEventDto;
import com.campusdual.cd2026mnd1g3.model.service.ProtocoloCiclicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints operativos del motor de protocolos cíclicos.
 */
@RestController
@RequestMapping("/api/protocolos-ciclicos")
public class ProtocoloCiclicoController {
    private final ProtocoloCiclicoService service;

    public ProtocoloCiclicoController(ProtocoloCiclicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProtocolEventDto> registrar(@RequestBody ProtocolEventDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarProtocolo(request));
    }

    @PostMapping("/animales/{animalId}/estado")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long animalId,
                                                  @RequestParam EstadoSanitario estado,
                                                  @RequestParam(required = false) String notas) {
        service.actualizarEstadoSanitario(animalId, estado, notas);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ejecutar-revision-diaria")
    public ResponseEntity<Void> ejecutarRevisionDiaria() {
        service.ejecutarRevisionDiaria();
        return ResponseEntity.noContent().build();
    }
}
