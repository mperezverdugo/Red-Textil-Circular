package com.textilcircular.mercado.controller;

import com.textilcircular.mercado.dto.*;
import com.textilcircular.mercado.security.AutenticacionFilter;
import com.textilcircular.mercado.security.TokenService.DatosToken;
import com.textilcircular.mercado.service.ConversacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/conversaciones")
public class ConversacionController {
    private final ConversacionService conversacionService;

    public ConversacionController(ConversacionService conversacionService) {
        this.conversacionService = conversacionService;
    }

    @PostMapping
    public ConversacionResponse crearOObtener(@Valid @RequestBody CrearConversacionRequest solicitud,
                                              HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return conversacionService.crearOObtener(usuario, solicitud);
    }

    @GetMapping
    public List<ConversacionResponse> listar(HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return conversacionService.listar(usuario);
    }

    @PostMapping("/{id}/mensajes")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse enviarMensaje(@PathVariable Long id,
                                         @Valid @RequestBody EnviarMensajeRequest solicitud,
                                         HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return conversacionService.enviarMensaje(id, usuario, solicitud);
    }

    @GetMapping("/{id}/mensajes")
    public List<MensajeResponse> listarMensajes(@PathVariable Long id,
                                                @RequestParam(required = false) Long despuesDeId,
                                                HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return conversacionService.listarMensajes(id, usuario, despuesDeId);
    }

    @PatchMapping("/{id}/lectura")
    public ConversacionResponse marcarLectura(@PathVariable Long id, HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return conversacionService.marcarLectura(id, usuario);
    }
}
