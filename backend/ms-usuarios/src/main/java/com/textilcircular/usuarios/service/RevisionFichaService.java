package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class RevisionFichaService {
    private final EmprendedoraRepository emprendedoras;

    public RevisionFichaService(EmprendedoraRepository emprendedoras) {
        this.emprendedoras = emprendedoras;
    }

    @Transactional
    public RevisionFichaResponse revisar(Long id, RevisionFichaRequest solicitud) {
        Emprendedora ficha = emprendedoras.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "EMPRENDEDORA_NO_ENCONTRADA", "La emprendedora no existe"));
        Usuario usuario = ficha.getUsuario();
        if (!ficha.isActiva() || usuario.getFechaBaja() != null || usuario.getFechaEliminacion() != null
                || ficha.getEstadoFicha() == EstadoFichaEmprendimiento.DADA_DE_BAJA) {
            throw transicionInvalida();
        }
        EstadoFichaEmprendimiento actual = ficha.getEstadoFicha();
        EstadoFichaEmprendimiento destino = switch (solicitud.accion()) {
            case APROBAR -> {
                if (actual != EstadoFichaEmprendimiento.POR_REVISAR && actual != EstadoFichaEmprendimiento.OCULTA) {
                    throw transicionInvalida();
                }
                yield EstadoFichaEmprendimiento.ACTIVA;
            }
            case RECHAZAR -> {
                if (actual != EstadoFichaEmprendimiento.POR_REVISAR) {
                    throw transicionInvalida();
                }
                yield EstadoFichaEmprendimiento.RECHAZADA;
            }
            case OCULTAR -> {
                if (actual != EstadoFichaEmprendimiento.ACTIVA) {
                    throw transicionInvalida();
                }
                yield EstadoFichaEmprendimiento.OCULTA;
            }
        };
        String motivo = solicitud.motivo() == null ? null : solicitud.motivo().strip();
        if (destino == EstadoFichaEmprendimiento.RECHAZADA && (motivo == null || motivo.isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MOTIVO_REQUERIDO",
                    "Debes indicar el motivo del rechazo");
        }
        ficha.revisar(destino, motivo == null || motivo.isBlank() ? null : motivo, Instant.now());
        return RevisionFichaResponse.desde(ficha);
    }

    private ApiException transicionInvalida() {
        return new ApiException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                "La acción no está permitida para el estado actual de la ficha");
    }
}
