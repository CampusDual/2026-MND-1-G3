package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AlertaSistemaDto;
import com.campusdual.cd2026mnd1g3.api.enums.DestinatarioRol;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.enums.TipoAlerta;
import com.campusdual.cd2026mnd1g3.model.entity.AlertaSistemaEntity;
import com.campusdual.cd2026mnd1g3.model.event.AlertaSistemaCreadaEvent;
import com.campusdual.cd2026mnd1g3.model.event.EstadoSanitarioCambiadoEvent;
import com.campusdual.cd2026mnd1g3.model.event.ProtocoloRegistradoEvent;
import com.campusdual.cd2026mnd1g3.model.repository.AlertaSistemaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
public class AlertaSistemaListener {
    private final AlertaSistemaRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public AlertaSistemaListener(AlertaSistemaRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProtocoloRegistrado(ProtocoloRegistradoEvent event) {
        if (event.conRetirada()) {
            saveAndPublish(event.clinicaId(), DestinatarioRol.FARM_MANAGER, TipoAlerta.FIN_RETIRADA_MEDICAMENTO,
                    "Fin de retirada de medicamento", "Finaliza la retirada para el animal " + event.animalId(),
                    event.fechaProgramada(), event.explotacionId(), event.animalId());
        }
        if (event.titulo() != null) {
            saveAndPublish(event.clinicaId(), DestinatarioRol.VETERINARIAN, TipoAlerta.RECORDATORIO_CICLICO,
                    "Protocolo registrado", event.titulo(), event.fechaProgramada(),
                    event.explotacionId(), event.animalId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEstadoSanitarioCambiado(EstadoSanitarioCambiadoEvent event) {
        if (event.nuevoEstado() == EstadoSanitario.ENFERMO) {
            saveAndPublish(event.clinicaId(), DestinatarioRol.VETERINARIAN, TipoAlerta.URGENCIA_ANIMAL_ENFERMO,
                    "Urgencia sanitaria", "El animal " + event.animalId() + " ha sido informado como enfermo.",
                    LocalDateTime.now(), event.explotacionId(), event.animalId());
        }
    }

    public void createStockAlert(Long clinicaId, Long explotacionId, Long animalId, String message) {
        saveAndPublish(clinicaId, DestinatarioRol.CLINIC_ADMIN, TipoAlerta.STOCK_BAJO,
                "Stock bajo", message, LocalDateTime.now(), explotacionId, animalId);
    }

    private void saveAndPublish(Long clinicaId, DestinatarioRol rol, TipoAlerta tipo,
                                String titulo, String mensaje, LocalDateTime fecha,
                                Long explotacionId, Long animalId) {
        AlertaSistemaEntity entity = new AlertaSistemaEntity();
        entity.setClinicaId(clinicaId);
        entity.setDestinatarioRol(rol);
        entity.setTipoAlerta(tipo);
        entity.setTitulo(titulo);
        entity.setMensaje(mensaje);
        entity.setFechaProgramada(fecha);
        entity.setExplotacionId(explotacionId);
        entity.setAnimalId(animalId);
        AlertaSistemaEntity saved = repository.save(entity);
        AlertaSistemaDto dto = toDto(saved);
        eventPublisher.publishEvent(new AlertaSistemaCreadaEvent(dto));
    }

    private AlertaSistemaDto toDto(AlertaSistemaEntity e) {
        return new AlertaSistemaDto(e.getId(), e.getClinicaId(), e.getDestinatarioRol(),
                e.getTipoAlerta(), e.getTitulo(), e.getMensaje(), e.getFechaProgramada(),
                e.isLeida(), e.isConfirmadaPorGranja(), e.getExplotacionId(), e.getAnimalId());
    }
}
