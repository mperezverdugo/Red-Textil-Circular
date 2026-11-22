package com.textilcircular.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearProductoRequest(
        @NotBlank(message = "El nombre del producto no puede estar vacío")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @NotBlank(message = "La descripción corta no puede estar vacía")
        @Size(min = 10, max = 250, message = "La descripción debe tener entre 10 y 250 caracteres")
        String descripcionCorta,

        @Size(max = 500, message = "La explicación del proceso no puede superar los 500 caracteres")
        String explicacionProceso
) {}
