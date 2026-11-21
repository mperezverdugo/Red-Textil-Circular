package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrarCliente(RegistroClienteRequest solicitud) {
        Validadores.validarCorreo(solicitud.email());
        Validadores.validarContrasena(solicitud.password());
        String email = normalizarCorreo(solicitud.email());
        comprobarCorreoDisponible(email);
        Usuario usuario = new Usuario(solicitud.nombre().strip(), email,
                passwordEncoder.encode(solicitud.password()), Rol.CLIENTE, solicitud.aceptaLeyDatos());
        return UsuarioResponse.desde(usuarios.save(usuario));
    }

    private void comprobarCorreoDisponible(String email) {
        if (usuarios.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "CORREO_DUPLICADO", "El correo ya está registrado");
        }
    }

    private String normalizarCorreo(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
