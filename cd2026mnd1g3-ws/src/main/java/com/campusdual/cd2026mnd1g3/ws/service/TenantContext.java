package com.campusdual.cd2026mnd1g3.ws.service;

import com.campusdual.cd2026mnd1g3.model.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {
    private final UserRepository userRepository;

    public TenantContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getClinicaId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return 1L;
        }
        return userRepository.findByUsername(authentication.getName())
                .map(user -> user.getClinicaId() == null ? 1L : user.getClinicaId())
                .orElse(1L);
    }

    public void validate(Long clinicaId) {
        if (clinicaId == null || !getClinicaId().equals(clinicaId)) {
            throw new AccessDeniedException("El recurso no pertenece a la clinica autenticada");
        }
    }
}
