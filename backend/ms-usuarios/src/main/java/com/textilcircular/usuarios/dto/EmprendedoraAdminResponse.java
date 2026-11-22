package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.Emprendedora;
import com.textilcircular.usuarios.model.EstadoFichaEmprendimiento;
import com.textilcircular.usuarios.model.Rubro;
import java.time.Instant;
import java.util.List;

public record EmprendedoraAdminResponse(
        Long id,
        Long usuarioId,
        String nombre,
        String email,
        String rut,
        String nombreComercial,
        String region,
        String comuna,
        List<Rubro> rubros,
        String instagram,
        boolean activa,
        EstadoFichaEmprendimiento estadoFicha,
        String motivoRevision,
        Instant fechaRevision) {

    public static EmprendedoraAdminResponse desde(Emprendedora ficha) {
        return new EmprendedoraAdminResponse(
                ficha.getId(),
                ficha.getUsuario().getId(),
                ficha.getUsuario().getNombre(),
                ficha.getUsuario().getEmail(),
                ficha.getRut(),
                ficha.getNombreComercial(),
                ficha.getRegion(),
                ficha.getComuna(),
                ficha.getRubros(),
                ficha.getInstagram(),
                ficha.isActiva(),
                ficha.getEstadoFicha(),
                ficha.getMotivoRevision(),
                ficha.getFechaRevision());
    }
}
