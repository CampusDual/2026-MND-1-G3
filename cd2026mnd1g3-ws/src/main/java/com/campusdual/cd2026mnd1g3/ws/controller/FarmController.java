package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.FarmDto;
import com.campusdual.cd2026mnd1g3.model.service.FarmService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/explotaciones")
public class FarmController {
    private final FarmService service;
    public FarmController(FarmService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<List<FarmDto>> findAll() { return ResponseEntity.ok(service.findAll()); }
    @GetMapping("/{id}")
    public ResponseEntity<FarmDto> findById(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }
    @PostMapping
    public ResponseEntity<FarmDto> create(@RequestBody FarmDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<FarmDto> update(@PathVariable Long id, @RequestBody FarmDto request) { return ResponseEntity.ok(service.update(id, request)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
