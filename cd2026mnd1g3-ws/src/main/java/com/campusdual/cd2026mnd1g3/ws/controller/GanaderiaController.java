package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.model.service.GanaderiaCommandService;
import com.campusdual.cd2026mnd1g3.model.service.GanaderiaQueryService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ganaderia")
public class GanaderiaController {
    private final GanaderiaCommandService commandService;
    private final GanaderiaQueryService queryService;
    private final TenantContext tenantContext;

    public GanaderiaController(GanaderiaCommandService commandService,
                               GanaderiaQueryService queryService,
                               TenantContext tenantContext) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/animales")
    public ResponseEntity<List<AnimalDto>> animales() {
        return ResponseEntity.ok(queryService.findAnimalesByClinica(tenantContext.getClinicaId()));
    }

    @PostMapping("/animales")
    public ResponseEntity<AnimalDto> crearAnimal(@RequestBody AnimalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.crearAnimal(dto));
    }

    @PostMapping("/animales/{animalId}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long animalId,
                                              @RequestParam EstadoSanitario estado,
                                              @RequestParam(required = false) String notas) {
        commandService.actualizarEstadoSanitario(tenantContext.getClinicaId(), animalId, estado, notas);
        return ResponseEntity.noContent().build();
    }
}
