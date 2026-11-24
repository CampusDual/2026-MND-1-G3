package com.campusdual.cd2026mnd1g3.api.service;

import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoResponseDto;

import java.util.List;

public interface GanaderiaFieldOperationService {
    List<GanaderiaOperacionCampoResponseDto> sync(
            Long clinicaId,
            List<GanaderiaOperacionCampoRequestDto> operaciones);
}
