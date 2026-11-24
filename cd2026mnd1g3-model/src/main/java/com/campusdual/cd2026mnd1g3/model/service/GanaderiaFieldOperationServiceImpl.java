package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoResponseDto;
import com.campusdual.cd2026mnd1g3.api.service.GanaderiaFieldOperationService;
import com.campusdual.cd2026mnd1g3.model.entity.GanaderiaOperacionCampoEntity;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import com.campusdual.cd2026mnd1g3.model.repository.GanaderiaOperacionCampoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GanaderiaFieldOperationServiceImpl implements GanaderiaFieldOperationService {
    private final GanaderiaOperacionCampoRepository operationRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;

    public GanaderiaFieldOperationServiceImpl(
            GanaderiaOperacionCampoRepository operationRepository,
            FarmRepository farmRepository,
            AnimalRepository animalRepository) {
        this.operationRepository = operationRepository;
        this.farmRepository = farmRepository;
        this.animalRepository = animalRepository;
    }

    @Override
    public List<GanaderiaOperacionCampoResponseDto> sync(
            Long clinicaId,
            List<GanaderiaOperacionCampoRequestDto> operaciones) {
        List<GanaderiaOperacionCampoResponseDto> responses = new ArrayList<>();
        for (GanaderiaOperacionCampoRequestDto request : operaciones) {
            if (request == null) continue;
            boolean farmValid = farmRepository.findById(request.farmId())
                    .filter(farm -> farm.getClinicaId().equals(clinicaId))
                    .isPresent();
            if (!farmValid) {
                throw new AccessDeniedException(
                        "La explotacion no pertenece a la clinica");
            }
            if (request.animalId() != null) {
                boolean animalValid = animalRepository.findById(request.animalId())
                        .filter(animal -> animal.getClinicaId().equals(clinicaId))
                        .filter(animal -> animal.getFarm().getId().equals(request.farmId()))
                        .isPresent();
                if (!animalValid) {
                    throw new AccessDeniedException(
                            "El animal no pertenece a la explotacion");
                }
            }

            GanaderiaOperacionCampoEntity operation =
                    operationRepository.findByClinicaIdAndOperationId(
                                    clinicaId, request.operationId())
                            .orElseGet(GanaderiaOperacionCampoEntity::new);
            operation.setOperationId(request.operationId());
            operation.setClinicaId(clinicaId);
            operation.setFarmId(request.farmId());
            operation.setAnimalId(request.animalId());
            operation.setTipoOperacion(request.tipoOperacion());
            operation.setDetalle(request.detalle());
            operation.setEstado("SINCRONIZADA");
            operation.setFechaSincronizacion(LocalDateTime.now());
            operation = operationRepository.save(operation);
            responses.add(toDto(operation));
        }
        return responses;
    }

    private GanaderiaOperacionCampoResponseDto toDto(
            GanaderiaOperacionCampoEntity operation) {
        return new GanaderiaOperacionCampoResponseDto(
                operation.getId(),
                operation.getOperationId(),
                operation.getFarmId(),
                operation.getAnimalId(),
                operation.getTipoOperacion(),
                operation.getDetalle(),
                operation.getEstado(),
                operation.getFechaCreacion(),
                operation.getFechaSincronizacion());
    }
}
