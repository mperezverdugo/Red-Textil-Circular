package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.*;
import java.time.Instant;
import java.util.List;

public record PerfilResponse(
        Long id, String nombre, String email, Rol rol, boolean aceptaLeyDatos,
        FichaResponse ficha) {

    public static PerfilResponse desde(Usuario usuario, Emprendedora ficha) {
        return new PerfilResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getRol(), usuario.isAceptaLeyDatos(), ficha == null ? null : FichaResponse.desde(ficha));
    }

    public record FichaResponse(
            Long id, String rut, String nombreComercial, String region, String comuna,
            List<Rubro> rubros, String instagram, boolean activa,
            EstadoFichaEmprendimiento estadoFicha, String motivoRevision, Instant fechaRevision,
            List<String> problemasDetectados) {

        public static FichaResponse desde(Emprendedora ficha) {
            return new FichaResponse(ficha.getId(), ficha.getRut(), ficha.getNombreComercial(),
                    ficha.getRegion(), ficha.getComuna(), ficha.getRubros(), ficha.getInstagram(),
                    ficha.isActiva(), ficha.getEstadoFicha(), ficha.getMotivoRevision(),
                    ficha.getFechaRevision(), ficha.getProblemasDetectados());
        }
    }
}
