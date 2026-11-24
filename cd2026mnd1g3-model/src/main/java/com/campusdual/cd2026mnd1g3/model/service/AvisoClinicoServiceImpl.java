package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AvisoClinicoDto;
import com.campusdual.cd2026mnd1g3.api.dto.AvisoClinicoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.CambioEstadoAvisoDto;
import com.campusdual.cd2026mnd1g3.api.dto.PageResponseDto;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoPaciente;
import com.campusdual.cd2026mnd1g3.api.enums.MotivoDescarteAviso;
import com.campusdual.cd2026mnd1g3.api.service.AvisoClinicoService;
import com.campusdual.cd2026mnd1g3.model.entity.AvisoClinicoEntity;
import com.campusdual.cd2026mnd1g3.model.entity.PacienteClinicaEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.AvisoClinicoRepository;
import com.campusdual.cd2026mnd1g3.model.repository.PacienteClinicaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AvisoClinicoServiceImpl implements AvisoClinicoService {
    private final AvisoClinicoRepository avisoRepository;
    private final PacienteClinicaRepository pacienteRepository;

    public AvisoClinicoServiceImpl(AvisoClinicoRepository avisoRepository,
                                   PacienteClinicaRepository pacienteRepository) {
        this.avisoRepository = avisoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AvisoClinicoDto> findAvisos(Long clinicaId, Pageable pageable) {
        Sort sort = pageable.getSort().isUnsorted()
                ? Sort.by(Sort.Direction.ASC, "fechaObjetivo")
                : pageable.getSort();
        Pageable deterministic = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                sort.and(Sort.by(Sort.Direction.ASC, "id")));
        Page<AvisoClinicoEntity> page = avisoRepository.findAll(
                (root, query, builder) -> builder.and(
                        builder.equal(root.get("clinicaId"), clinicaId),
                        builder.isTrue(root.get("activo"))),
                deterministic);
        return new PageResponseDto<>(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public AvisoClinicoDto crear(Long clinicaId, AvisoClinicoRequestDto request) {
        PacienteClinicaEntity paciente = pacienteRepository
                .findByIdAndClinicaIdAndActivoTrue(request.pacienteId(), clinicaId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", request.pacienteId()));
        AvisoClinicoEntity aviso = new AvisoClinicoEntity();
        aviso.setClinicaId(clinicaId);
        aviso.setPaciente(paciente);
        aviso.setPropietario(paciente.getPropietario());
        aviso.setCategoria(request.categoria());
        aviso.setTitulo(request.titulo());
        aviso.setDetalleClinico(request.detalleClinico());
        aviso.setFechaObjetivo(request.fechaObjetivo());
        aviso.setFechaInicio(request.fechaInicio());
        aviso.setFechaFin(request.fechaFin());
        aviso.setProgreso(request.progreso());
        aviso.setEstado(EstadoAvisoClinico.PENDIENTE);
        return toDto(avisoRepository.save(aviso));
    }

    @Override
    public AvisoClinicoDto marcarIntentado(Long clinicaId, Long avisoId, String usuario) {
        AvisoClinicoEntity aviso = findActive(clinicaId, avisoId);
        aviso.setEstado(EstadoAvisoClinico.INTENTADO);
        aviso.setIntentos(aviso.getIntentos() + 1);
        aviso.setUltimoIntento(LocalDateTime.now());
        aviso.setUltimoUsuario(usuario);
        aviso.setFechaActualizacion(LocalDateTime.now());
        return toDto(avisoRepository.save(aviso));
    }

    @Override
    public AvisoClinicoDto posponer(Long clinicaId, Long avisoId,
                                    LocalDate nuevaFecha, String usuario) {
        if (nuevaFecha == null || nuevaFecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La nueva fecha debe ser valida");
        }
        AvisoClinicoEntity aviso = findActive(clinicaId, avisoId);
        aviso.setEstado(EstadoAvisoClinico.POSPUESTA);
        aviso.setFechaObjetivo(nuevaFecha);
        aviso.setUltimoUsuario(usuario);
        aviso.setFechaActualizacion(LocalDateTime.now());
        return toDto(avisoRepository.save(aviso));
    }

    @Override
    public AvisoClinicoDto cambiarEstado(Long clinicaId, Long avisoId,
                                         CambioEstadoAvisoDto request, String usuario) {
        AvisoClinicoEntity aviso = findActive(clinicaId, avisoId);
        aviso.setUltimoUsuario(usuario);
        aviso.setFechaActualizacion(LocalDateTime.now());

        if (request.estado() == EstadoAvisoClinico.DESCARTADA) {
            if (request.motivoDescarte() == null) {
                throw new IllegalArgumentException("El motivo de descarte es obligatorio");
            }
            aviso.setEstado(EstadoAvisoClinico.DESCARTADA);
            aviso.setMotivoDescarte(request.motivoDescarte());
            if (request.motivoDescarte() == MotivoDescarteAviso.FALLECIMIENTO_BAJA) {
                if (!Boolean.TRUE.equals(request.confirmarFallecimiento())) {
                    throw new IllegalArgumentException(
                            "Se requiere doble confirmacion para fallecimiento/baja");
                }
                PacienteClinicaEntity paciente = aviso.getPaciente();
                paciente.setActivo(false);
                paciente.setEstadoPaciente(EstadoPaciente.INACTIVO);
                aviso.setActivo(false);
                aviso.setCanceladoPorBaja(true);
                avisoRepository.cancelarPendientesPorBaja(
                        clinicaId,
                        paciente.getId(),
                        List.of(EstadoAvisoClinico.PENDIENTE,
                                EstadoAvisoClinico.INTENTADO,
                                EstadoAvisoClinico.CONTACTADO,
                                EstadoAvisoClinico.POSPUESTA));
            }
        } else if (request.estado() == EstadoAvisoClinico.POSPUESTA) {
            if (request.nuevaFecha() == null) {
                throw new IllegalArgumentException("La fecha pospuesta es obligatoria");
            }
            aviso.setFechaObjetivo(request.nuevaFecha());
            aviso.setEstado(EstadoAvisoClinico.POSPUESTA);
        } else {
            aviso.setEstado(request.estado());
        }
        return toDto(avisoRepository.save(aviso));
    }

    private AvisoClinicoEntity findActive(Long clinicaId, Long avisoId) {
        return avisoRepository.findByIdAndClinicaId(avisoId, clinicaId)
                .filter(AvisoClinicoEntity::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("AvisoClinico", avisoId));
    }

    private AvisoClinicoDto toDto(AvisoClinicoEntity aviso) {
        PacienteClinicaEntity paciente = aviso.getPaciente();
        var owner = paciente.getPropietario();
        return new AvisoClinicoDto(
                aviso.getId(), aviso.getClinicaId(), paciente.getId(), owner.getId(),
                paciente.getNombre(),
                owner.getNombre() + " " + owner.getApellidos(),
                owner.getTelefono(), owner.getEmail(),
                aviso.getCategoria(), aviso.getTitulo(), aviso.getDetalleClinico(),
                aviso.getFechaObjetivo(), aviso.getEstado(), aviso.getIntentos(),
                aviso.getUltimoIntento(), aviso.getUltimoUsuario(),
                aviso.getFechaInicio(), aviso.getFechaFin(), aviso.getProgreso(),
                aviso.getMotivoDescarte(), aviso.isActivo());
    }
}
