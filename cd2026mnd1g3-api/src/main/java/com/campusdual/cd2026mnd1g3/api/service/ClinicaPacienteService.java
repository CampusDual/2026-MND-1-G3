package com.campusdual.cd2026mnd1g3.api.service;

import com.campusdual.cd2026mnd1g3.api.dto.*;
import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClinicaPacienteService {
    PageResponseDto<PacienteClinicaDto> findPacientes(
            Long clinicaId, String propietario, String nombre, String microchip,
            Boolean sinMicrochip, EspecieMascota especie, String raza, Pageable pageable);

    PacienteClinicaDto findPaciente(Long clinicaId, Long pacienteId);

    List<PropietarioClinicaDto> buscarPropietarios(Long clinicaId, String termino);

    PropietarioClinicaDto buscarDuplicadoPropietario(Long clinicaId, String telefono, String dniNif);

    List<PacienteClinicaDto> findMascotasPropietario(Long clinicaId, Long propietarioId);

    List<String> findRazas(Long clinicaId, EspecieMascota especie);

    AltaPacienteResponseDto altaPaciente(Long clinicaId, AltaPacienteRequestDto request);
}
