package com.textilcircular.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.textilcircular.usuarios.model.Rubro;
import jakarta.validation.constraints.*;
import java.util.List;

public record EditarPerfilRequest(
        @Size(max = 255) @Pattern(regexp = "(?s).*\\S.*", message = "El nombre no puede estar vacío") String nombre,
        @Size(max = 255) @Pattern(regexp = "(?s).*\\S.*", message = "El nombre comercial no puede estar vacío") String nombreComercial,
        @Size(max = 255) @Pattern(regexp = "(?s).*\\S.*", message = "La región no puede estar vacía") String region,
        @Size(max = 255) @Pattern(regexp = "(?s).*\\S.*", message = "La comuna no puede estar vacía") String comuna,
        @Size(min = 1, message = "Debes indicar al menos un rubro") List<@NotNull Rubro> rubros,
        @Size(max = 30) String instagram) {

    @JsonAnySetter
    public void rechazarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("El campo " + campo + " no se puede editar en el perfil");
    }
}
