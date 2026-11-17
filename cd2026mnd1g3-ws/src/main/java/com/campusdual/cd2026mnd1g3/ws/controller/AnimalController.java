package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.model.service.AnimalService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/animales")
public class AnimalController {
    private final AnimalService service;
    public AnimalController(AnimalService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<List<AnimalDto>> findAll() { return ResponseEntity.ok(service.findAll()); }
    @GetMapping("/{id}")
    public ResponseEntity<AnimalDto> findById(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }
    @PostMapping
    public ResponseEntity<AnimalDto> create(@RequestBody AnimalDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
