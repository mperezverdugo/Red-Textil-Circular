package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.Emprendedora;
import com.textilcircular.usuarios.model.EstadoFichaEmprendimiento;
import java.time.Instant;

public record RevisionFichaResponse(
        Long id, EstadoFichaEmprendimiento estadoFicha, String motivoRevision, Instant fechaRevision) {

    public static RevisionFichaResponse desde(Emprendedora ficha) {
        return new RevisionFichaResponse(ficha.getId(), ficha.getEstadoFicha(),
                ficha.getMotivoRevision(), ficha.getFechaRevision());
    }
}
