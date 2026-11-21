package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final EmprendedoraRepository emprendedoras;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                       EmprendedoraRepository emprendedoras) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.emprendedoras = emprendedoras;
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

    @Transactional
    public RegistroEmprendedoraResponse registrarEmprendedora(RegistroEmprendedoraRequest solicitud) {
        Validadores.validarCorreo(solicitud.email());
        Validadores.validarContrasena(solicitud.password());
        Validadores.validarRut(solicitud.rut());
        String email = normalizarCorreo(solicitud.email());
        String rut = Validadores.limpiarRut(solicitud.rut());
        comprobarCorreoDisponible(email);
        if (emprendedoras.existsByRut(rut)) {
            throw new ApiException(HttpStatus.CONFLICT, "RUT_DUPLICADO", "El RUT ya está registrado");
        }
        Usuario usuario = usuarios.save(new Usuario(solicitud.nombre().strip(), email,
                passwordEncoder.encode(solicitud.password()), Rol.EMPRENDEDORA, solicitud.aceptaLeyDatos()));
        Emprendedora ficha = emprendedoras.save(new Emprendedora(usuario, rut,
                solicitud.nombreComercial().strip(), solicitud.region().strip(), solicitud.comuna().strip(),
                solicitud.rubros(), solicitud.instagram()));
        return new RegistroEmprendedoraResponse(UsuarioResponse.desde(usuario), ficha.getId(), ficha.getEstadoFicha());
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
