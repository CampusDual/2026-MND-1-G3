package com.campusdual.cd2026mnd1g3.api.service;

import com.campusdual.cd2026mnd1g3.api.dto.AvisoClinicoDto;
import com.campusdual.cd2026mnd1g3.api.dto.AvisoClinicoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.CambioEstadoAvisoDto;
import com.campusdual.cd2026mnd1g3.api.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface AvisoClinicoService {
    PageResponseDto<AvisoClinicoDto> findAvisos(Long clinicaId, Pageable pageable);
    AvisoClinicoDto crear(Long clinicaId, AvisoClinicoRequestDto request);
    AvisoClinicoDto cambiarEstado(Long clinicaId, Long avisoId, CambioEstadoAvisoDto request, String usuario);
    AvisoClinicoDto posponer(Long clinicaId, Long avisoId, java.time.LocalDate nuevaFecha, String usuario);
    AvisoClinicoDto marcarIntentado(Long clinicaId, Long avisoId, String usuario);
}
