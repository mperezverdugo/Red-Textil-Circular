package com.textilcircular.usuarios.controller;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.security.AutenticacionFilter;
import com.textilcircular.usuarios.security.TokenService.DatosToken;
import com.textilcircular.usuarios.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios/me")
public class UsuarioController {
    private final PerfilService perfiles;

    public UsuarioController(PerfilService perfiles) {
        this.perfiles = perfiles;
    }

    @GetMapping
    public PerfilResponse obtener(
            @RequestAttribute(AutenticacionFilter.USUARIO_AUTENTICADO) DatosToken usuario) {
        return perfiles.obtener(usuario.usuarioId());
    }

    @PatchMapping
    public PerfilResponse editar(
            @RequestAttribute(AutenticacionFilter.USUARIO_AUTENTICADO) DatosToken usuario,
            @Valid @RequestBody EditarPerfilRequest solicitud) {
        return perfiles.editar(usuario.usuarioId(), solicitud);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(
            @RequestAttribute(AutenticacionFilter.USUARIO_AUTENTICADO) DatosToken usuario) {
        perfiles.darDeBaja(usuario.usuarioId());
    }
}
