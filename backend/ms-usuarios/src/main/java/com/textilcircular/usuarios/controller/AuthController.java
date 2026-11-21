package com.textilcircular.usuarios.controller;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro/emprendedora")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroEmprendedoraResponse registrarEmprendedora(
            @Valid @RequestBody RegistroEmprendedoraRequest solicitud) {
        return authService.registrarEmprendedora(solicitud);
    }

    @PostMapping("/registro/cliente")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrarCliente(@Valid @RequestBody RegistroClienteRequest solicitud) {
        return authService.registrarCliente(solicitud);
    }
}
