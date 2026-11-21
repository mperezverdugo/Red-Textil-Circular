package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.EstadoFichaEmprendimiento;

public record RegistroEmprendedoraResponse(
        UsuarioResponse usuario, Long emprendedoraId, EstadoFichaEmprendimiento estadoFicha) {
}
