package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import com.textilcircular.usuarios.security.TokenService;
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
    private final TokenService tokenService;
    private final String hashCuentaInexistente;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                       EmprendedoraRepository emprendedoras, TokenService tokenService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.emprendedoras = emprendedoras;
        this.tokenService = tokenService;
        this.hashCuentaInexistente = passwordEncoder.encode("comparacion-para-cuenta-inexistente");
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
        return registrarEmprendedoraInterno(solicitud, EstadoFichaEmprendimiento.POR_REVISAR);
    }

    @Transactional
    public Emprendedora registrarEmprendedoraComoAdmin(RegistroEmprendedoraRequest solicitud) {
        RegistroEmprendedoraResponse respuesta = registrarEmprendedoraInterno(solicitud, EstadoFichaEmprendimiento.ACTIVA);
        return emprendedoras.findById(respuesta.emprendedoraId()).orElseThrow();
    }

    private RegistroEmprendedoraResponse registrarEmprendedoraInterno(
            RegistroEmprendedoraRequest solicitud, EstadoFichaEmprendimiento estadoInicial) {
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
        Emprendedora ficha = new Emprendedora(usuario, rut,
                solicitud.nombreComercial().strip(), solicitud.region().strip(), solicitud.comuna().strip(),
                solicitud.rubros(), solicitud.instagram());
        if (estadoInicial != EstadoFichaEmprendimiento.POR_REVISAR) {
            ficha.revisar(estadoInicial, null, null);
        }
        ficha = emprendedoras.save(ficha);
        return new RegistroEmprendedoraResponse(UsuarioResponse.desde(usuario), ficha.getId(), ficha.getEstadoFicha());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest solicitud) {
        Usuario usuario = usuarios.findByEmail(normalizarCorreo(solicitud.email())).orElse(null);
        // También compara un hash cuando el correo no existe.
        String hash = usuario == null ? hashCuentaInexistente : usuario.getPasswordHash();
        boolean coincide = passwordEncoder.matches(solicitud.password(), hash);
        if (usuario == null || !coincide) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                    "Correo o contraseña incorrectos");
        }
        if (usuario.getFechaBaja() != null || usuario.getFechaEliminacion() != null) {
            throw new ApiException(HttpStatus.LOCKED, "CUENTA_DADA_DE_BAJA", "La cuenta está dada de baja");
        }
        if (usuario.getRol() == Rol.EMPRENDEDORA) {
            Emprendedora ficha = emprendedoras.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                            "FICHA_NO_DISPONIBLE", "La ficha de la emprendedora no está disponible"));
            comprobarEstadoFicha(ficha);
        }
        String token = tokenService.generar(usuario.getId(), usuario.getRol());
        return new LoginResponse(token, UsuarioResponse.desde(usuario));
    }

    private void comprobarEstadoFicha(Emprendedora ficha) {
        if (!ficha.isActiva() || ficha.getEstadoFicha() == EstadoFichaEmprendimiento.DADA_DE_BAJA) {
            throw new ApiException(HttpStatus.LOCKED, "CUENTA_DADA_DE_BAJA", "La cuenta está dada de baja");
        }
        switch (ficha.getEstadoFicha()) {
            case POR_REVISAR -> throw new ApiException(HttpStatus.FORBIDDEN, "CUENTA_POR_REVISAR",
                    "Tu registro está pendiente de revisión");
            case RECHAZADA -> throw new ApiException(HttpStatus.FORBIDDEN, "CUENTA_RECHAZADA",
                    "Tu registro fue rechazado");
            case ACTIVA, OCULTA -> { }
            default -> throw new ApiException(HttpStatus.FORBIDDEN, "FICHA_NO_DISPONIBLE",
                    "La ficha de la emprendedora no está disponible");
        }
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
