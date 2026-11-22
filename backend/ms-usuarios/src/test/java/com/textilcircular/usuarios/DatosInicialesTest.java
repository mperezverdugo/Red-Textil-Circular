package com.textilcircular.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.config.DatosIniciales;
import com.textilcircular.usuarios.dto.LoginRequest;
import com.textilcircular.usuarios.model.EstadoFichaEmprendimiento;
import com.textilcircular.usuarios.model.Rol;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DatosInicialesTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired DatosIniciales datosIniciales;

    @Test
    void cargaSemillaCorrectamente() {
        datosIniciales.sembrarSiEstaVacia();

        assertThat(usuarios.count()).isEqualTo(15);
        assertThat(emprendedoras.count()).isEqualTo(12);

        // Cuenta ADMIN
        var admin = usuarios.findByEmail("admin@textilcircular.cl").orElseThrow();
        assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);

        // Emprendedora Activa
        var camila = usuarios.findByEmail("camila.morales@textilcircular.cl").orElseThrow();
        var fichaCamila = emprendedoras.findByUsuarioId(camila.getId()).orElseThrow();
        assertThat(fichaCamila.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.ACTIVA);

        // Emprendedora Pendiente por revisar
        var patricia = usuarios.findByEmail("patricia.valenzuela@correo.cl").orElseThrow();
        var fichaPatricia = emprendedoras.findByUsuarioId(patricia.getId()).orElseThrow();
        assertThat(fichaPatricia.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.POR_REVISAR);

        // Emprendedora Rechazada con motivo
        var monica = usuarios.findByEmail("monica.caceres@correo.cl").orElseThrow();
        var fichaMonica = emprendedoras.findByUsuarioId(monica.getId()).orElseThrow();
        assertThat(fichaMonica.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.RECHAZADA);
        assertThat(fichaMonica.getMotivoRevision()).isNotBlank();

        // Clientes
        var francisca = usuarios.findByEmail("francisca.tapia@correo.cl").orElseThrow();
        assertThat(francisca.getRol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void noDuplicaDatosAlEjecutarOtraVez() {
        datosIniciales.sembrarSiEstaVacia();
        long totalUsuarios = usuarios.count();
        long totalFichas = emprendedoras.count();

        datosIniciales.sembrarSiEstaVacia();

        assertThat(usuarios.count()).isEqualTo(totalUsuarios);
        assertThat(emprendedoras.count()).isEqualTo(totalFichas);
    }

    @Test
    void cuentaAdminPuedeIniciarSesion() throws Exception {
        datosIniciales.sembrarSiEstaVacia();

        LoginRequest loginAdmin = new LoginRequest("admin@textilcircular.cl", DatosIniciales.PASSWORD_ADMIN);

        mvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(loginAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.email").value("admin@textilcircular.cl"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }
}
