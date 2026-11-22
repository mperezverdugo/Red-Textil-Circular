package com.textilcircular.mercado.dto;

import com.textilcircular.mercado.model.Producto;

public record ProductoResponse(
        Long id,
        Long emprendedoraId,
        String nombre,
        String descripcionCorta,
        String explicacionProceso
) {
    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getEmprendedoraId(),
                producto.getNombre(),
                producto.getDescripcionCorta(),
                producto.getExplicacionProceso()
        );
    }
}
