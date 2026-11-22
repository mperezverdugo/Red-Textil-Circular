package com.textilcircular.usuarios;

import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import com.textilcircular.usuarios.security.TokenService;
import jakarta.persistence.EntityManager;
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
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PerfilTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired TokenService tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;

    private Usuario crearUsuario(Rol rol) {
        return usuarios.save(new Usuario("Ana", "ana@example.cl", encoder.encode("secreto"), rol, true));
    }

    private Usuario crearEmprendedora() {
        Usuario usuario = crearUsuario(Rol.EMPRENDEDORA);
        Emprendedora ficha = new Emprendedora(usuario, "123456785", "Taller Ana", "Metropolitana",
                "Santiago", List.of(Rubro.REPARACION), "taller.ana");
        ficha.revisar(EstadoFichaEmprendimiento.ACTIVA, "Antecedentes revisados",
                Instant.parse("2026-11-21T12:00:00Z"));
        emprendedoras.save(ficha);
        return usuario;
    }

    private String autorizacion(Usuario usuario) {
        return "Bearer " + tokens.generar(usuario.getId(), usuario.getRol());
    }

    @Test
    void obtienePerfilDelClienteSinCredenciales() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        mvc.perform(get("/usuarios/me").header("Authorization", autorizacion(usuario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(usuario.getId()))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@example.cl"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.ficha").isEmpty());
    }

    @Test
    void obtieneFichaConEstadoMotivoYColecciones() throws Exception {
        Usuario usuario = crearEmprendedora();
        String token = autorizacion(usuario);
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/usuarios/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ficha.rut").value("123456785"))
                .andExpect(jsonPath("$.ficha.nombreComercial").value("Taller Ana"))
                .andExpect(jsonPath("$.ficha.estadoFicha").value("ACTIVA"))
                .andExpect(jsonPath("$.ficha.motivoRevision").value("Antecedentes revisados"))
                .andExpect(jsonPath("$.ficha.fechaRevision").value("2026-11-21T12:00:00Z"))
                .andExpect(jsonPath("$.ficha.rubros[0]").value("REPARACION"))
                .andExpect(jsonPath("$.ficha.problemasDetectados").isArray());
    }

    @Test
    void rechazaOperacionesSinToken() throws Exception {
        mvc.perform(get("/usuarios/me")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/usuarios/me").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void editaSoloNombreYConservaCredenciales() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        String hash = usuario.getPasswordHash();
        mvc.perform(patch("/usuarios/me").header("Authorization", autorizacion(usuario))
                .contentType("application/json").content("{\"nombre\":\" Ana María \"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Ana María"));
        entityManager.flush();
        entityManager.clear();
        Usuario guardado = usuarios.findById(usuario.getId()).orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("Ana María");
        assertThat(guardado.getEmail()).isEqualTo("ana@example.cl");
        assertThat(guardado.getPasswordHash()).isEqualTo(hash);
    }

    @Test
    void modificaFichaYConservaNombreRutYRevision() throws Exception {
        Usuario usuario = crearEmprendedora();
        mvc.perform(patch("/usuarios/me").header("Authorization", autorizacion(usuario))
                .contentType("application/json").content("""
                {"nombreComercial":"Nuevo taller","region":"Valparaíso","comuna":"Viña del Mar",
                 "rubros":["TALLERES","ARRIENDO"],"instagram":"nuevo.taller"}
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.ficha.nombreComercial").value("Nuevo taller"))
                .andExpect(jsonPath("$.ficha.region").value("Valparaíso"))
                .andExpect(jsonPath("$.ficha.comuna").value("Viña del Mar"))
                .andExpect(jsonPath("$.ficha.rubros.length()").value(2))
                .andExpect(jsonPath("$.ficha.instagram").value("nuevo.taller"))
                .andExpect(jsonPath("$.ficha.rut").value("123456785"))
                .andExpect(jsonPath("$.ficha.estadoFicha").value("ACTIVA"));
        entityManager.flush();
        entityManager.clear();
        assertThat(emprendedoras.findByUsuarioId(usuario.getId()).orElseThrow().getNombreComercial())
                .isEqualTo("Nuevo taller");
    }

    @Test
    void conservaCamposOmitidosYPermiteQuitarInstagram() throws Exception {
        Usuario usuario = crearEmprendedora();
        String token = autorizacion(usuario);
        mvc.perform(patch("/usuarios/me").header("Authorization", token)
                .contentType("application/json").content("{\"nombreComercial\":\"Otro taller\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ficha.region").value("Metropolitana"))
                .andExpect(jsonPath("$.ficha.comuna").value("Santiago"))
                .andExpect(jsonPath("$.ficha.rubros[0]").value("REPARACION"))
                .andExpect(jsonPath("$.ficha.instagram").value("taller.ana"));
        mvc.perform(patch("/usuarios/me").header("Authorization", token)
                .contentType("application/json").content("{\"instagram\":\"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ficha.instagram").isEmpty())
                .andExpect(jsonPath("$.ficha.nombreComercial").value("Otro taller"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"nombre\":\" \"}", "{\"nombreComercial\":\"\"}", "{\"region\":\"\"}",
            "{\"comuna\":\" \"}", "{\"rubros\":[]}", "{\"rubros\":[null]}",
            "{\"rubros\":[\"DESCONOCIDO\"]}", "{\"instagram\":\"1234567890123456789012345678901\"}"
    })
    void rechazaValoresInvalidosSinCambiarPerfil(String cuerpo) throws Exception {
        Usuario usuario = crearEmprendedora();
        mvc.perform(patch("/usuarios/me").header("Authorization", autorizacion(usuario))
                .contentType("application/json").content(cuerpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        assertThat(usuario.getNombre()).isEqualTo("Ana");
        assertThat(emprendedoras.findByUsuarioId(usuario.getId()).orElseThrow().getNombreComercial())
                .isEqualTo("Taller Ana");
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "rut", "password", "rol"})
    void rechazaCamposNoEditables(String campo) throws Exception {
        Usuario usuario = crearEmprendedora();
        mvc.perform(patch("/usuarios/me").header("Authorization", autorizacion(usuario))
                .contentType("application/json").content("{\"" + campo + "\":\"otro\"}"))
                .andExpect(status().isBadRequest());
        assertThat(usuario.getEmail()).isEqualTo("ana@example.cl");
        assertThat(usuario.getRol()).isEqualTo(Rol.EMPRENDEDORA);
    }

    @Test
    void noPermiteElegirOtraCuenta() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        Usuario otra = usuarios.save(new Usuario("Eva", "eva@example.cl", "hash", Rol.CLIENTE, true));
        String token = autorizacion(usuario);
        mvc.perform(patch("/usuarios/me").header("Authorization", token)
                .contentType("application/json")
                .content("{\"id\":" + otra.getId() + ",\"nombre\":\"Cambio\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/usuarios/me").header("Authorization", token)
                .contentType("application/json").content("{\"nombre\":\"Cambio propio\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(usuario.getId()));
        assertThat(usuarios.findById(otra.getId()).orElseThrow().getNombre()).isEqualTo("Eva");
    }

    @Test
    void bajaLogicaBloqueaLoginYTokenAnterior() throws Exception {
        Usuario usuario = crearEmprendedora();
        String token = autorizacion(usuario);
        mvc.perform(delete("/usuarios/me").header("Authorization", token))
                .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        Usuario guardado = usuarios.findById(usuario.getId()).orElseThrow();
        Emprendedora ficha = emprendedoras.findByUsuarioId(usuario.getId()).orElseThrow();
        assertThat(guardado.getFechaBaja()).isNotNull();
        assertThat(guardado.getFechaEliminacion()).isNull();
        assertThat(ficha.isActiva()).isFalse();
        assertThat(ficha.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.DADA_DE_BAJA);
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"email\":\"ana@example.cl\",\"password\":\"secreto\"}"))
                .andExpect(status().isLocked()).andExpect(jsonPath("$.codigo").value("CUENTA_DADA_DE_BAJA"));
        mvc.perform(get("/usuarios/me").header("Authorization", token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("CUENTA_DADA_DE_BAJA"));
    }

    @Test
    void clienteNoPuedeSolicitarBajaDeEmprendedora() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        mvc.perform(delete("/usuarios/me").header("Authorization", autorizacion(usuario)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        assertThat(usuario.getFechaBaja()).isNull();
    }

    @Test
    void clienteNoPuedeEditarCamposDeFicha() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        mvc.perform(patch("/usuarios/me").header("Authorization", autorizacion(usuario))
                .contentType("application/json").content("{\"nombreComercial\":\"Taller\"}"))
                .andExpect(status().isBadRequest());
    }
}
