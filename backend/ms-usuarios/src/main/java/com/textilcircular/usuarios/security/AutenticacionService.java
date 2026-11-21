package com.textilcircular.usuarios.security;

import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {
    private final TokenService tokens;
    private final UsuarioRepository usuarios;
    private final EmprendedoraRepository emprendedoras;

    public AutenticacionService(TokenService tokens, UsuarioRepository usuarios,
                                EmprendedoraRepository emprendedoras) {
        this.tokens = tokens;
        this.usuarios = usuarios;
        this.emprendedoras = emprendedoras;
    }

    @Transactional(readOnly = true)
    public TokenService.DatosToken autenticar(String token) {
        TokenService.DatosToken datos;
        try {
            datos = tokens.validar(token);
        } catch (IllegalArgumentException error) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "El token es inválido o ha vencido");
        }
        Usuario usuario = usuarios.findById(datos.usuarioId()).orElseThrow(() ->
                new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "La cuenta del token no existe"));
        if (usuario.getFechaBaja() != null || usuario.getFechaEliminacion() != null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CUENTA_DADA_DE_BAJA", "La cuenta está dada de baja");
        }
        if (usuario.getRol() != datos.rol()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "El rol del token ya no es válido");
        }
        if (usuario.getRol() == Rol.EMPRENDEDORA) {
            Emprendedora ficha = emprendedoras.findByUsuarioId(usuario.getId()).orElseThrow(() ->
                    new ApiException(HttpStatus.FORBIDDEN, "CUENTA_NO_HABILITADA", "La cuenta no está habilitada"));
            if (!ficha.isActiva() || ficha.getEstadoFicha() == EstadoFichaEmprendimiento.DADA_DE_BAJA) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "CUENTA_DADA_DE_BAJA", "La cuenta está dada de baja");
            }
            if (ficha.getEstadoFicha() != EstadoFichaEmprendimiento.ACTIVA
                    && ficha.getEstadoFicha() != EstadoFichaEmprendimiento.OCULTA) {
                throw new ApiException(HttpStatus.FORBIDDEN, "CUENTA_NO_HABILITADA", "La cuenta no está habilitada");
            }
        }
        return datos;
    }
}
