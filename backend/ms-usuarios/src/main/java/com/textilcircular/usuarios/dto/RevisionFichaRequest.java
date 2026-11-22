package com.textilcircular.usuarios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevisionFichaRequest(
        @NotNull(message = "Debes indicar una acción") Accion accion,
        @Size(max = 2000, message = "El motivo no puede superar los 2000 caracteres") String motivo) {

    public enum Accion {
        APROBAR, RECHAZAR, OCULTAR
    }
}
