package com.textilcircular.mercado.service;

import com.textilcircular.mercado.dto.*;
import com.textilcircular.mercado.exception.ApiException;
import com.textilcircular.mercado.model.*;
import com.textilcircular.mercado.repository.ConversacionRepository;
import com.textilcircular.mercado.repository.MensajeRepository;
import com.textilcircular.mercado.security.TokenService.DatosToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ConversacionService {
    private final ConversacionRepository conversaciones;
    private final MensajeRepository mensajes;

    public ConversacionService(ConversacionRepository conversaciones, MensajeRepository mensajes) {
        this.conversaciones = conversaciones;
        this.mensajes = mensajes;
    }

    @Transactional
    public ConversacionResponse crearOObtener(DatosToken usuario, CrearConversacionRequest solicitud) {
        if (usuario.rol() != Rol.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "Solo los clientes pueden iniciar conversaciones");
        }
        Optional<Conversacion> existente = conversaciones.findByClienteIdAndEmprendedoraId(
                usuario.usuarioId(), solicitud.emprendedoraId());
        if (existente.isPresent()) {
            return ConversacionResponse.desde(existente.get());
        }
        Conversacion nueva = new Conversacion(
                usuario.usuarioId(),
                solicitud.emprendedoraId(),
                solicitud.nombreCliente().strip(),
                solicitud.nombreEmprendimiento().strip(),
                solicitud.productoId(),
                Instant.now()
        );
        return ConversacionResponse.desde(conversaciones.save(nueva));
    }

    @Transactional(readOnly = true)
    public List<ConversacionResponse> listar(DatosToken usuario) {
        return conversaciones.listarPorUsuario(usuario.usuarioId())
                .stream().map(ConversacionResponse::desde).toList();
    }

    @Transactional
    public MensajeResponse enviarMensaje(Long conversacionId, DatosToken usuario, EnviarMensajeRequest solicitud) {
        Conversacion c = buscar(conversacionId);
        TipoEmisor emisor = validarParticipante(c, usuario);

        String texto = solicitud.contenido() == null ? "" : solicitud.contenido().strip();
        if (texto.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El mensaje no puede estar vacío");
        }
        if (texto.length() > 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El mensaje no puede superar los 1000 caracteres");
        }

        Instant ahora = Instant.now();
        Mensaje mensaje = new Mensaje(
                c.getId(),
                emisor,
                texto,
                ahora,
                false,
                solicitud.productoId(),
                solicitud.nombreProducto()
        );
        mensaje = mensajes.save(mensaje);
        c.registrarMensaje(texto, ahora, emisor == TipoEmisor.CLIENTE);
        return MensajeResponse.desde(mensaje);
    }

    @Transactional(readOnly = true)
    public List<MensajeResponse> listarMensajes(Long conversacionId, DatosToken usuario, Long despuesDeId) {
        Conversacion c = buscar(conversacionId);
        validarParticipante(c, usuario);

        List<Mensaje> resultado = (despuesDeId != null)
                ? mensajes.findByConversacionIdAndIdGreaterThanOrderByIdAsc(conversacionId, despuesDeId)
                : mensajes.findByConversacionIdOrderByIdAsc(conversacionId);

        return resultado.stream().map(MensajeResponse::desde).toList();
    }

    @Transactional
    public ConversacionResponse marcarLectura(Long conversacionId, DatosToken usuario) {
        Conversacion c = buscar(conversacionId);
        TipoEmisor emisor = validarParticipante(c, usuario);

        if (emisor == TipoEmisor.CLIENTE) {
            c.marcarLeidaPorCliente();
            mensajes.marcarLeidosPorEmisor(conversacionId, TipoEmisor.EMPRENDEDORA);
        } else {
            c.marcarLeidaPorEmprendedora();
            mensajes.marcarLeidosPorEmisor(conversacionId, TipoEmisor.CLIENTE);
        }
        return ConversacionResponse.desde(c);
    }

    private Conversacion buscar(Long id) {
        return conversaciones.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "CONVERSACION_NO_ENCONTRADA", "La conversación no existe"));
    }

    private TipoEmisor validarParticipante(Conversacion c, DatosToken usuario) {
        if (usuario.usuarioId().equals(c.getClienteId())) {
            return TipoEmisor.CLIENTE;
        }
        if (usuario.usuarioId().equals(c.getEmprendedoraId())) {
            return TipoEmisor.EMPRENDEDORA;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tienes acceso a esta conversación");
    }
}
