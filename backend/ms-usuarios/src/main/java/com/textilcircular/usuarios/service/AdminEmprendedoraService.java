package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class AdminEmprendedoraService {
    private final EmprendedoraRepository emprendedoras;
    private final AuthService authService;

    public AdminEmprendedoraService(EmprendedoraRepository emprendedoras, AuthService authService) {
        this.emprendedoras = emprendedoras;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<EmprendedoraAdminResponse> listar(String estado) {
        EstadoFichaEmprendimiento estadoFiltro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                estadoFiltro = EstadoFichaEmprendimiento.valueOf(estado.strip());
            } catch (IllegalArgumentException error) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El estado de la ficha no es válido");
            }
        }
        return emprendedoras.listarParaAdmin(estadoFiltro)
                .stream().map(EmprendedoraAdminResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public EmprendedoraAdminResponse obtener(Long id) {
        Emprendedora ficha = buscarFicha(id);
        return EmprendedoraAdminResponse.desde(ficha);
    }

    @Transactional
    public EmprendedoraAdminResponse crear(RegistroEmprendedoraRequest solicitud) {
        Emprendedora ficha = authService.registrarEmprendedoraComoAdmin(solicitud);
        return EmprendedoraAdminResponse.desde(ficha);
    }

    @Transactional
    public EmprendedoraAdminResponse editar(Long id, EditarFichaAdminRequest solicitud) {
        Emprendedora ficha = buscarFicha(id);
        String instagram = solicitud.instagram() == null ? null : solicitud.instagram().strip();
        if (instagram != null && instagram.isEmpty()) {
            instagram = null;
        }
        ficha.editarDatos(
                solicitud.nombreComercial().strip(),
                solicitud.region().strip(),
                solicitud.comuna().strip(),
                solicitud.rubros(),
                instagram);
        return EmprendedoraAdminResponse.desde(ficha);
    }

    @Transactional
    public void eliminar(Long id) {
        Emprendedora ficha = buscarFicha(id);
        ficha.eliminar(Instant.now());
    }

    private Emprendedora buscarFicha(Long id) {
        Emprendedora ficha = emprendedoras.findById(id).orElseThrow(this::noEncontrada);
        if (ficha.getUsuario().getFechaEliminacion() != null) {
            throw noEncontrada();
        }
        return ficha;
    }

    private ApiException noEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "EMPRENDEDORA_NO_ENCONTRADA",
                "La emprendedora no existe");
    }
}
