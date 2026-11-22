package com.textilcircular.mercado.dto;

import com.textilcircular.mercado.model.Conversacion;
import java.time.Instant;

public record ConversacionResponse(
        Long id,
        Long clienteId,
        Long emprendedoraId,
        Long productoId,
        String nombreCliente,
        String nombreEmprendimiento,
        String ultimoMensaje,
        Instant fechaHora,
        int noLeidosCliente,
        int noLeidosEmprendedora
) {
    public static ConversacionResponse desde(Conversacion c) {
        return new ConversacionResponse(
                c.getId(),
                c.getClienteId(),
                c.getEmprendedoraId(),
                c.getProductoId(),
                c.getNombreCliente(),
                c.getNombreEmprendimiento(),
                c.getUltimoMensaje(),
                c.getFechaHora(),
                c.getNoLeidosCliente(),
                c.getNoLeidosEmprendedora()
        );
    }
}
