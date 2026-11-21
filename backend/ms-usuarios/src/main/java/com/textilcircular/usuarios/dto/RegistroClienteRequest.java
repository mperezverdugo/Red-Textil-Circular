package com.textilcircular.usuarios.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroClienteRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 255) String nombre,
        @NotBlank(message = "El correo es obligatorio") @Size(max = 255) String email,
        String password,
        @AssertTrue(message = "Debes aceptar la ley de datos") boolean aceptaLeyDatos) {
}
