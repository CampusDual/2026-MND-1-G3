package com.campusdual.cd2026mnd1g3.api.service;

import com.campusdual.cd2026mnd1g3.api.dto.*;

import java.util.List;

public interface ClinicaConsultaService {
    List<ConsultaClinicaDto> findConsultas(Long clinicaId);
    ConsultaClinicaDto crearConsulta(Long clinicaId, AltaConsultaClinicaRequestDto request);
    List<RecetaClinicaDto> findRecetas(Long clinicaId);
    RecetaClinicaDto crearReceta(Long clinicaId, AltaRecetaClinicaRequestDto request);
}
