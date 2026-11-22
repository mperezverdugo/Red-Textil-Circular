package com.textilcircular.mercado.dto;

import com.textilcircular.mercado.model.Mensaje;
import com.textilcircular.mercado.model.TipoEmisor;
import java.time.Instant;

public record MensajeResponse(
        Long id,
        Long conversacionId,
        TipoEmisor emisor,
        String contenido,
        Instant fechaHora,
        boolean esLeido,
        Long productoId,
        String nombreProducto
) {
    public static MensajeResponse desde(Mensaje m) {
        return new MensajeResponse(
                m.getId(),
                m.getConversacionId(),
                m.getEmisor(),
                m.getContenido(),
                m.getFechaHora(),
                m.isEsLeido(),
                m.getProductoId(),
                m.getNombreProducto()
        );
    }
}
