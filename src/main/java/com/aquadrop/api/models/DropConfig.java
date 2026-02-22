package com.aquadrop.api.models;

/**
 * DropConfig record: Es inmutable e incorpora las reglas de negocio base de
 * validación.
 */
public record DropConfig(String sourceId, String dropId, float probability, int quantity) {
    public DropConfig {
        if (probability < 0.0f || probability > 100.0f) {
            throw new IllegalArgumentException(
                    "La probabilidad debe estar estrictamente entre 0.0f y 100.0f. Valor recibido: " + probability);
        }
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId no puede ser nulo o estar vacío.");
        }
        if (dropId == null || dropId.isBlank()) {
            throw new IllegalArgumentException("dropId no puede ser nulo o estar vacío.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
    }
}
