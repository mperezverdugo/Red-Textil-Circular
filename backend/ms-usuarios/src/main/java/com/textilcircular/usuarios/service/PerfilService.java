package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class PerfilService {
    private final UsuarioRepository usuarios;
    private final EmprendedoraRepository emprendedoras;

    public PerfilService(UsuarioRepository usuarios, EmprendedoraRepository emprendedoras) {
        this.usuarios = usuarios;
        this.emprendedoras = emprendedoras;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtener(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Emprendedora ficha = usuario.getRol() == Rol.EMPRENDEDORA ? buscarFicha(usuarioId) : null;
        return PerfilResponse.desde(usuario, ficha);
    }

    @Transactional
    public PerfilResponse editar(Long usuarioId, EditarPerfilRequest solicitud) {
        Usuario usuario = buscarUsuario(usuarioId);
        Emprendedora ficha = null;
        if (usuario.getRol() == Rol.EMPRENDEDORA) {
            ficha = buscarFicha(usuarioId);
            String instagram = solicitud.instagram() == null ? ficha.getInstagram() : solicitud.instagram().strip();
            if (instagram != null && instagram.isEmpty()) {
                instagram = null;
            }
            ficha.editarDatos(
                    conservarOActualizar(solicitud.nombreComercial(), ficha.getNombreComercial()),
                    conservarOActualizar(solicitud.region(), ficha.getRegion()),
                    conservarOActualizar(solicitud.comuna(), ficha.getComuna()),
                    solicitud.rubros() == null ? ficha.getRubros() : solicitud.rubros(),
                    instagram);
        } else if (solicitud.nombreComercial() != null || solicitud.region() != null
                || solicitud.comuna() != null || solicitud.rubros() != null || solicitud.instagram() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS",
                    "Solo una emprendedora puede editar los datos de su ficha");
        }
        if (solicitud.nombre() != null) {
            usuario.cambiarNombre(solicitud.nombre().strip());
        }
        return PerfilResponse.desde(usuario, ficha);
    }

    @Transactional
    public void darDeBaja(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        if (usuario.getRol() != Rol.EMPRENDEDORA) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "Solo una emprendedora puede solicitar esta baja");
        }
        buscarFicha(usuarioId).darDeBaja(Instant.now());
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarios.findById(usuarioId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", "El usuario no existe"));
    }

    private Emprendedora buscarFicha(Long usuarioId) {
        return emprendedoras.findByUsuarioId(usuarioId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "FICHA_NO_ENCONTRADA", "La ficha no existe"));
    }

    private String conservarOActualizar(String nuevo, String actual) {
        return nuevo == null ? actual : nuevo.strip();
    }
}
