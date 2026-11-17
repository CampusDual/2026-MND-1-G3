package com.campusdual.cd2026mnd1g3.model.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " no encontrado: " + id);
    }
}
