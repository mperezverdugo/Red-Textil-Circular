package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.Emprendedora;
import com.textilcircular.usuarios.model.Rubro;
import java.util.List;

public record EmprendedoraPublicaResponse(
        Long id, String nombreComercial, String region, String comuna,
        List<Rubro> rubros, String instagram) {

    public static EmprendedoraPublicaResponse desde(Emprendedora ficha) {
        return new EmprendedoraPublicaResponse(ficha.getId(), ficha.getNombreComercial(),
                ficha.getRegion(), ficha.getComuna(), ficha.getRubros(), ficha.getInstagram());
    }
}
