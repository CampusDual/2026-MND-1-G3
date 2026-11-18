package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.AlertaSistemaDto;
import com.campusdual.cd2026mnd1g3.model.service.AlertaSistemaService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaSistemaController {
    private final AlertaSistemaService service;
    private final TenantContext tenantContext;

    public AlertaSistemaController(AlertaSistemaService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ResponseEntity<List<AlertaSistemaDto>> findAll() {
        return ResponseEntity.ok(service.findByClinica(tenantContext.getClinicaId()));
    }

    @PostMapping("/{id}/leida")
    public ResponseEntity<AlertaSistemaDto> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarLeida(tenantContext.getClinicaId(), id));
    }

    @PostMapping("/{id}/confirmar-granja")
    public ResponseEntity<AlertaSistemaDto> confirmarPorGranja(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmarPorGranja(tenantContext.getClinicaId(), id));
    }
}
