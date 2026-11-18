package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.MedicamentoDto;
import com.campusdual.cd2026mnd1g3.api.dto.MovimientoInventarioDto;
import com.campusdual.cd2026mnd1g3.api.dto.LoteStockDto;
import com.campusdual.cd2026mnd1g3.api.enums.TipoMovimiento;
import com.campusdual.cd2026mnd1g3.model.service.InventarioService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final InventarioService service;
    private final TenantContext tenantContext;

    public InventarioController(InventarioService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/medicamentos")
    public ResponseEntity<List<MedicamentoDto>> medicamentos() {
        return ResponseEntity.ok(service.findMedicamentos(tenantContext.getClinicaId()));
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<LoteStockDto>> lotes() {
        return ResponseEntity.ok(service.findLotes(tenantContext.getClinicaId()));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoInventarioDto>> movimientos() {
        return ResponseEntity.ok(service.findMovimientos(tenantContext.getClinicaId()));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventarioDto> registrarSalida(@RequestParam Long loteStockId,
                                                                    @RequestParam double cantidad,
                                                                    @RequestParam TipoMovimiento tipo,
                                                                    @RequestParam(required = false) Long veterinarioId,
                                                                    @RequestParam(required = false) Long animalId,
                                                                    @RequestParam(required = false) Long explotacionId,
                                                                    @RequestParam(required = false) Long protocoloEventoId) {
        return ResponseEntity.ok(service.registrarSalida(tenantContext.getClinicaId(),
                loteStockId, cantidad, tipo, veterinarioId, animalId, explotacionId, protocoloEventoId));
    }
}
