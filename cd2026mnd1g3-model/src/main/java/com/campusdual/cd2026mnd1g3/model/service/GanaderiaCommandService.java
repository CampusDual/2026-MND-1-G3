package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GanaderiaCommandService {
    private final AnimalService animalService;
    private final ProtocoloCiclicoService protocoloService;

    public GanaderiaCommandService(AnimalService animalService, ProtocoloCiclicoService protocoloService) {
        this.animalService = animalService;
        this.protocoloService = protocoloService;
    }

    public AnimalDto crearAnimal(AnimalDto dto) {
        return animalService.save(dto);
    }

    public void actualizarEstadoSanitario(Long clinicaId, Long animalId, EstadoSanitario estado, String notas) {
        protocoloService.actualizarEstadoSanitario(animalId, estado, notas);
    }
}
