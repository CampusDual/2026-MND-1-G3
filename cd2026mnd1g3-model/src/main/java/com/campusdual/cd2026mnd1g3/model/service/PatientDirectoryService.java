package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.PatientDetailDto;
import com.campusdual.cd2026mnd1g3.api.dto.PatientPageDto;
import com.campusdual.cd2026mnd1g3.api.enums.Species;
import org.springframework.data.domain.Pageable;

public interface PatientDirectoryService {
    PatientPageDto search(Long clinicaId, String ownerName, String petName, String microchip,
                          boolean withoutMicrochip, Species species, String breed, Pageable pageable);
    PatientDetailDto detail(Long clinicaId, Long patientId);
}
