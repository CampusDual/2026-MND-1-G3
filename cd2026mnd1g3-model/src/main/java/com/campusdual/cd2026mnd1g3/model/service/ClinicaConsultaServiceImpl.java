package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.*;
import com.campusdual.cd2026mnd1g3.api.service.ClinicaConsultaService;
import com.campusdual.cd2026mnd1g3.model.entity.*;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClinicaConsultaServiceImpl implements ClinicaConsultaService {
    private final ConsultaClinicaRepository consultaRepository;
    private final RecetaClinicaRepository recetaRepository;
    private final PacienteClinicaRepository pacienteRepository;

    public ClinicaConsultaServiceImpl(ConsultaClinicaRepository consultaRepository,
                                      RecetaClinicaRepository recetaRepository,
                                      PacienteClinicaRepository pacienteRepository) {
        this.consultaRepository = consultaRepository;
        this.recetaRepository = recetaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaClinicaDto> findConsultas(Long clinicaId) {
        return consultaRepository.findByClinicaIdOrderByFechaConsultaDesc(clinicaId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ConsultaClinicaDto crearConsulta(Long clinicaId, AltaConsultaClinicaRequestDto request) {
        PacienteClinicaEntity paciente = pacienteRepository
                .findByIdAndClinicaIdAndActivoTrue(request.pacienteId(), clinicaId)
                .orElseThrow(() -> new AccessDeniedException(
                        "El paciente no pertenece a la clinica"));
        ConsultaClinicaEntity consulta = new ConsultaClinicaEntity();
        consulta.setClinicaId(clinicaId);
        consulta.setPaciente(paciente);
        consulta.setPropietario(paciente.getPropietario());
        consulta.setFechaConsulta(request.fechaConsulta());
        consulta.setMotivo(request.motivo());
        consulta.setNotas(request.notas());
        consulta.setProximaVacuna(request.proximaVacuna());
        return toDto(consultaRepository.save(consulta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecetaClinicaDto> findRecetas(Long clinicaId) {
        return recetaRepository.findByClinicaIdOrderByFechaEmisionDesc(clinicaId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public RecetaClinicaDto crearReceta(Long clinicaId, AltaRecetaClinicaRequestDto request) {
        PacienteClinicaEntity paciente = pacienteRepository
                .findByIdAndClinicaIdAndActivoTrue(request.pacienteId(), clinicaId)
                .orElseThrow(() -> new AccessDeniedException(
                        "El paciente no pertenece a la clinica"));
        ConsultaClinicaEntity consulta = consultaRepository
                .findByIdAndClinicaId(request.consultaId(), clinicaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", request.consultaId()));
        if (!consulta.getPaciente().getId().equals(paciente.getId())) {
            throw new IllegalArgumentException(
                    "La receta debe pertenecer al paciente de la consulta");
        }
        RecetaClinicaEntity receta = new RecetaClinicaEntity();
        receta.setClinicaId(clinicaId);
        receta.setConsulta(consulta);
        receta.setPaciente(paciente);
        receta.setMedicamentoId(request.medicamentoId());
        receta.setLoteStockId(request.loteStockId());
        receta.setIndicaciones(request.indicaciones());
        receta.setFechaEmision(request.fechaEmision());
        return toDto(recetaRepository.save(receta));
    }

    private ConsultaClinicaDto toDto(ConsultaClinicaEntity consulta) {
        PacienteClinicaEntity paciente = consulta.getPaciente();
        PropietarioClinicaEntity owner = consulta.getPropietario();
        return new ConsultaClinicaDto(
                consulta.getId(), consulta.getClinicaId(), paciente.getId(),
                paciente.getNombre(), paciente.getEspecie().name(),
                paciente.getRaza(), paciente.getMicrochip(),
                owner.getId(), owner.getNombre(), owner.getApellidos(),
                consulta.getFechaConsulta(), consulta.getMotivo(),
                consulta.getNotas(), consulta.getProximaVacuna());
    }

    private RecetaClinicaDto toDto(RecetaClinicaEntity receta) {
        return new RecetaClinicaDto(
                receta.getId(), receta.getClinicaId(), receta.getConsulta().getId(),
                receta.getPaciente().getId(), receta.getPaciente().getNombre(),
                receta.getMedicamentoId(), receta.getLoteStockId(),
                receta.getIndicaciones(), receta.getFechaEmision());
    }
}
