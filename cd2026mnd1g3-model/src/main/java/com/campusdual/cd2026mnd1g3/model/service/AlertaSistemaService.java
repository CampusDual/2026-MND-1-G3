package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AlertaSistemaDto;
import com.campusdual.cd2026mnd1g3.model.entity.AlertaSistemaEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.AlertaSistemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertaSistemaService {
    private final AlertaSistemaRepository repository;

    public AlertaSistemaService(AlertaSistemaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AlertaSistemaDto> findByClinica(Long clinicaId) {
        return repository.findByClinicaIdOrderByFechaProgramadaDesc(clinicaId).stream()
                .map(this::toDto).toList();
    }

    public AlertaSistemaDto marcarLeida(Long clinicaId, Long id) {
        AlertaSistemaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertaSistema", id));
        if (!entity.getClinicaId().equals(clinicaId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado");
        }
        entity.setLeida(true);
        return toDto(repository.save(entity));
    }

    public AlertaSistemaDto confirmarPorGranja(Long clinicaId, Long id) {
        AlertaSistemaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertaSistema", id));
        if (!entity.getClinicaId().equals(clinicaId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado");
        }
        entity.setConfirmadaPorGranja(true);
        return toDto(repository.save(entity));
    }

    private AlertaSistemaDto toDto(AlertaSistemaEntity e) {
        return new AlertaSistemaDto(e.getId(), e.getClinicaId(), e.getDestinatarioRol(),
                e.getTipoAlerta(), e.getTitulo(), e.getMensaje(), e.getFechaProgramada(),
                e.isLeida(), e.isConfirmadaPorGranja(), e.getExplotacionId(), e.getAnimalId());
    }
}
