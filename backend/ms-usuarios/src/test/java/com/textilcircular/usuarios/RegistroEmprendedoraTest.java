package com.textilcircular.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistroEmprendedoraTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired PasswordEncoder encoder;

    private RegistroEmprendedoraRequest solicitud(String email, String rut) {
        return new RegistroEmprendedoraRequest("Ana", email, "secreto", true, rut,
                "Taller Ana", "Metropolitana", "Santiago", List.of(Rubro.REPARACION), null);
    }

    @Test
    void registraFichaPendienteSinTokenNiCredenciales() throws Exception {
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud(" ANA@example.cl ", "12.345.678-5"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoFicha").value("POR_REVISAR"))
                .andExpect(jsonPath("$.usuario.rol").value("EMPRENDEDORA"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
        var usuario = usuarios.findByEmail("ana@example.cl").orElseThrow();
        assertThat(encoder.matches("secreto", usuario.getPasswordHash())).isTrue();
        var ficha = emprendedoras.findByUsuarioId(usuario.getId()).orElseThrow();
        assertThat(ficha.getRut()).isEqualTo("123456785");
        assertThat(ficha.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.POR_REVISAR);
    }

    @Test
    void rechazaRutDuplicadoAunqueCambieElFormato() throws Exception {
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud("ana@example.cl", "12.345.678-5"))))
                .andExpect(status().isCreated());
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud("otra@example.cl", "123456785"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("RUT_DUPLICADO"));
        assertThat(usuarios.findByEmail("otra@example.cl")).isEmpty();
    }

    @Test
    void rechazaCorreoUsadoPorCliente() throws Exception {
        usuarios.save(new Usuario("Cliente", "ana@example.cl", "hash", Rol.CLIENTE, true));
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud(" ANA@example.cl ", "123456785"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CORREO_DUPLICADO"));
        assertThat(emprendedoras.existsByRut("123456785")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678-9", "abc"})
    void rechazaRutInvalidoSinCrearUsuario(String rut) throws Exception {
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud("ana@example.cl", rut))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        assertThat(usuarios.findByEmail("ana@example.cl")).isEmpty();
    }

    @Test
    void rechazaFichaSinRubros() throws Exception {
        var solicitud = new RegistroEmprendedoraRequest("Ana", "ana@example.cl", "secreto", true,
                "123456785", "Taller", "RM", "Santiago", List.of(), null);
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest());
        assertThat(usuarios.findByEmail("ana@example.cl")).isEmpty();
    }
}
