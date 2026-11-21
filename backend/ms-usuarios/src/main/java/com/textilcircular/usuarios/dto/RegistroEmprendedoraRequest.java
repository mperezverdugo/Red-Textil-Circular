package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.Rubro;
import jakarta.validation.constraints.*;
import java.util.List;

public record RegistroEmprendedoraRequest(
        @NotBlank @Size(max = 255) String nombre,
        @NotBlank @Size(max = 255) String email,
        String password,
        @AssertTrue(message = "Debes aceptar la ley de datos") boolean aceptaLeyDatos,
        String rut,
        @NotBlank @Size(max = 255) String nombreComercial,
        @NotBlank @Size(max = 255) String region,
        @NotBlank @Size(max = 255) String comuna,
        @NotEmpty List<@NotNull Rubro> rubros,
        @Size(max = 30) String instagram) {
}
