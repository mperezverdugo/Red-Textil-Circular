package com.textilcircular.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearConversacionRequest(
        @NotNull(message = "Debes indicar la emprendedora")
        Long emprendedoraId,

        @NotBlank(message = "El nombre del cliente no puede estar vacío")
        @Size(max = 255, message = "El nombre del cliente no puede superar los 255 caracteres")
        String nombreCliente,

        @NotBlank(message = "El nombre del emprendimiento no puede estar vacío")
        @Size(max = 255, message = "El nombre del emprendimiento no puede superar los 255 caracteres")
        String nombreEmprendimiento,

        Long productoId
) {}
