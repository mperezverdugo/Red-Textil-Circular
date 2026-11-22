package com.textilcircular.usuarios.service;

import com.textilcircular.usuarios.dto.EmprendedoraPublicaResponse;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EmprendedoraService {
    private final EmprendedoraRepository emprendedoras;

    public EmprendedoraService(EmprendedoraRepository emprendedoras) {
        this.emprendedoras = emprendedoras;
    }

    @Transactional(readOnly = true)
    public List<EmprendedoraPublicaResponse> listar(String texto, String region, String comuna, String rubro) {
        Rubro rubroFiltro = null;
        String nombreRubro = limpiarFiltro(rubro);
        if (nombreRubro != null) {
            try {
                rubroFiltro = Rubro.valueOf(nombreRubro);
            } catch (IllegalArgumentException error) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El rubro no es válido");
            }
        }
        return emprendedoras.buscarActivas(EstadoFichaEmprendimiento.ACTIVA,
                        limpiarFiltro(texto), limpiarFiltro(region), limpiarFiltro(comuna), rubroFiltro)
                .stream().map(EmprendedoraPublicaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public EmprendedoraPublicaResponse obtener(Long id) {
        Emprendedora ficha = emprendedoras.findById(id)
                .orElseThrow(this::noEncontrada);
        Usuario usuario = ficha.getUsuario();
        if (ficha.getEstadoFicha() != EstadoFichaEmprendimiento.ACTIVA || !ficha.isActiva()
                || usuario.getFechaBaja() != null || usuario.getFechaEliminacion() != null) {
            throw noEncontrada();
        }
        return EmprendedoraPublicaResponse.desde(ficha);
    }

    private String limpiarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    private ApiException noEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "EMPRENDEDORA_NO_ENCONTRADA",
                "La emprendedora no está disponible");
    }
}
