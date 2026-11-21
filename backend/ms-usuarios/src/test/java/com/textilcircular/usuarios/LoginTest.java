package com.textilcircular.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import com.textilcircular.usuarios.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoginTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired PasswordEncoder encoder;
    @Autowired TokenService tokens;

    private Usuario crearUsuario(Rol rol) {
        return usuarios.save(new Usuario("Ana", "ana@example.cl", encoder.encode("secreto"), rol, true));
    }

    private ResultActions entrar(String email, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType("application/json")
                .content(json.writeValueAsString(new LoginRequest(email, password))));
    }

    private Emprendedora crearFicha(EstadoFichaEmprendimiento estado) {
        Usuario usuario = crearUsuario(Rol.EMPRENDEDORA);
        Emprendedora ficha = new Emprendedora(usuario, "123456785", "Taller", "RM",
                "Santiago", List.of(Rubro.REPARACION), null);
        ficha.revisar(estado, "Revisión de prueba", Instant.now());
        return emprendedoras.save(ficha);
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = {"CLIENTE", "ADMIN"})
    void devuelveTokenValidoYDatosMinimos(Rol rol) throws Exception {
        Usuario usuario = crearUsuario(rol);
        String respuesta = entrar(" ANA@example.cl ", "secreto")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
                .andExpect(jsonPath("$.usuario.rol").value(rol.name()))
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(respuesta).get("token").asText();
        assertThat(tokens.validar(token, rol).usuarioId()).isEqualTo(usuario.getId());
    }

    @Test
    void correoInexistenteYContrasenaIncorrectaDanLaMismaRespuesta() throws Exception {
        crearUsuario(Rol.CLIENTE);
        String incorrecta = entrar("ana@example.cl", "incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"))
                .andReturn().getResponse().getContentAsString();
        String inexistente = entrar("nadie@example.cl", "incorrecta")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(incorrecta).isEqualTo(inexistente);
    }

    @Test
    void bloqueaEmprendedoraRecienRegistrada() throws Exception {
        var solicitud = new RegistroEmprendedoraRequest("Ana", "ana@example.cl", "secreto", true,
                "123456785", "Taller", "RM", "Santiago", List.of(Rubro.REPARACION), null);
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(solicitud))).andExpect(status().isCreated());
        entrar("ana@example.cl", "secreto").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CUENTA_POR_REVISAR"))
                .andExpect(jsonPath("$.mensaje").value("Tu registro está pendiente de revisión"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void bloqueaFichaRechazada() throws Exception {
        crearFicha(EstadoFichaEmprendimiento.RECHAZADA);
        entrar("ana@example.cl", "secreto").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CUENTA_RECHAZADA"))
                .andExpect(jsonPath("$.mensaje").value("Tu registro fue rechazado"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void bloqueaBajaLogica() throws Exception {
        crearFicha(EstadoFichaEmprendimiento.ACTIVA).darDeBaja(Instant.now());
        entrar("ana@example.cl", "secreto").andExpect(status().isLocked())
                .andExpect(jsonPath("$.codigo").value("CUENTA_DADA_DE_BAJA"))
                .andExpect(jsonPath("$.mensaje").value("La cuenta está dada de baja"));
    }

    @Test
    void bloqueaEliminacionLogica() throws Exception {
        crearUsuario(Rol.CLIENTE).eliminar(Instant.now());
        entrar("ana@example.cl", "secreto").andExpect(status().isLocked())
                .andExpect(jsonPath("$.codigo").value("CUENTA_DADA_DE_BAJA"));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoFichaEmprendimiento.class, names = {"ACTIVA", "OCULTA"})
    void permiteFichaAprobadaAunqueEsteOculta(EstadoFichaEmprendimiento estado) throws Exception {
        crearFicha(estado);
        entrar("ana@example.cl", "secreto").andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void noRevelaEstadoConContrasenaIncorrecta() throws Exception {
        crearFicha(EstadoFichaEmprendimiento.RECHAZADA);
        entrar("ana@example.cl", "incorrecta").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void bloqueaEmprendedoraSinFicha() throws Exception {
        crearUsuario(Rol.EMPRENDEDORA);
        entrar("ana@example.cl", "secreto").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FICHA_NO_DISPONIBLE"));
    }

    @Test
    void rechazaSolicitudVacia() throws Exception {
        entrar("", "").andExpect(status().isBadRequest());
    }
}
