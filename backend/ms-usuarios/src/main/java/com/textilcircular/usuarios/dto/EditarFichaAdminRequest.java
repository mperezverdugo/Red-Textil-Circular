package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.Rubro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EditarFichaAdminRequest(
        @NotBlank @Size(max = 255) String nombreComercial,
        @NotBlank @Size(max = 255) String region,
        @NotBlank @Size(max = 255) String comuna,
        @NotEmpty List<@NotNull Rubro> rubros,
        @Size(max = 30) String instagram) {
}
