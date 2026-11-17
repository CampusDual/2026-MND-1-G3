package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.ProtocolEventDto;
import com.campusdual.cd2026mnd1g3.model.service.ProtocolEventService;
import com.campusdual.cd2026mnd1g3.model.service.ProtocoloCiclicoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/protocolos")
public class ProtocolEventController {
    private final ProtocolEventService service;
    private final ProtocoloCiclicoService cicloService;
    public ProtocolEventController(ProtocolEventService service, ProtocoloCiclicoService cicloService) {
        this.service = service;
        this.cicloService = cicloService;
    }
    @GetMapping
    public ResponseEntity<List<ProtocolEventDto>> findAll() { return ResponseEntity.ok(service.findAll()); }
    @PostMapping
    public ResponseEntity<ProtocolEventDto> create(@RequestBody ProtocolEventDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cicloService.registrarProtocolo(request));
    }
}
