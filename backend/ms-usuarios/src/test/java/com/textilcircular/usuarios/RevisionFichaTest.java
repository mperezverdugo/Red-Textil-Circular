package com.textilcircular.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.dto.RegistroEmprendedoraRequest;
import com.textilcircular.usuarios.dto.RevisionFichaRequest;
import com.textilcircular.usuarios.dto.RevisionFichaRequest.Accion;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import com.textilcircular.usuarios.security.TokenService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RevisionFichaTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired TokenService tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;

    private String tokenAdmin() {
        Usuario admin = usuarios.save(new Usuario("Admin", "admin@example.cl", "hash", Rol.ADMIN, true));
        return "Bearer " + tokens.generar(admin.getId(), Rol.ADMIN);
    }

    private Emprendedora crearFicha(EstadoFichaEmprendimiento estado) {
        Usuario usuario = usuarios.save(new Usuario("Ana", "ana@example.cl",
                encoder.encode("secreto"), Rol.EMPRENDEDORA, true));
        Emprendedora ficha = new Emprendedora(usuario, "123456785", "Taller", "RM",
                "Santiago", List.of(Rubro.REPARACION), null);
        ficha.revisar(estado, "Motivo anterior", Instant.parse("2026-01-01T00:00:00Z"));
        return emprendedoras.save(ficha);
    }

    static Stream<Arguments> transicionesValidas() {
        return Stream.of(
                Arguments.of(EstadoFichaEmprendimiento.POR_REVISAR, Accion.APROBAR, EstadoFichaEmprendimiento.ACTIVA),
                Arguments.of(EstadoFichaEmprendimiento.POR_REVISAR, Accion.RECHAZAR, EstadoFichaEmprendimiento.RECHAZADA),
                Arguments.of(EstadoFichaEmprendimiento.ACTIVA, Accion.OCULTAR, EstadoFichaEmprendimiento.OCULTA),
                Arguments.of(EstadoFichaEmprendimiento.OCULTA, Accion.APROBAR, EstadoFichaEmprendimiento.ACTIVA),
                Arguments.of(EstadoFichaEmprendimiento.RECHAZADA, Accion.APROBAR, EstadoFichaEmprendimiento.ACTIVA));
    }

    @ParameterizedTest
    @MethodSource("transicionesValidas")
    void guardaEstadoMotivoYFecha(EstadoFichaEmprendimiento origen, Accion accion,
                                  EstadoFichaEmprendimiento destino) throws Exception {
        Emprendedora ficha = crearFicha(origen);
        Instant antes = Instant.now();
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .header("Authorization", tokenAdmin()).contentType("application/json")
                .content(json.writeValueAsString(new RevisionFichaRequest(accion, " Motivo nuevo ")) ))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estadoFicha").value(destino.name()))
                .andExpect(jsonPath("$.motivoRevision").value("Motivo nuevo"))
                .andExpect(jsonPath("$.fechaRevision").isString());
        entityManager.flush();
        entityManager.clear();
        Emprendedora guardada = emprendedoras.findById(ficha.getId()).orElseThrow();
        assertThat(guardada.getEstadoFicha()).isEqualTo(destino);
        assertThat(guardada.getMotivoRevision()).isEqualTo("Motivo nuevo");
        assertThat(guardada.getFechaRevision()).isBetween(antes.minusMillis(1), Instant.now().plusMillis(100));
    }

    static Stream<Arguments> transicionesInvalidas() {
        List<Arguments> casos = new ArrayList<>();
        for (EstadoFichaEmprendimiento estado : EstadoFichaEmprendimiento.values()) {
            for (Accion accion : Accion.values()) {
                boolean valida = (estado == EstadoFichaEmprendimiento.POR_REVISAR
                        && (accion == Accion.APROBAR || accion == Accion.RECHAZAR))
                        || (estado == EstadoFichaEmprendimiento.ACTIVA && accion == Accion.OCULTAR)
                        || (estado == EstadoFichaEmprendimiento.OCULTA && accion == Accion.APROBAR)
                        || (estado == EstadoFichaEmprendimiento.RECHAZADA && accion == Accion.APROBAR);
                if (!valida) casos.add(Arguments.of(estado, accion));
            }
        }
        return casos.stream();
    }

    @ParameterizedTest
    @MethodSource("transicionesInvalidas")
    void rechazaTransicionSinAlterarFicha(EstadoFichaEmprendimiento estado, Accion accion) throws Exception {
        Emprendedora ficha = crearFicha(estado);
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .header("Authorization", tokenAdmin()).contentType("application/json")
                .content(json.writeValueAsString(new RevisionFichaRequest(accion, "Motivo"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
        assertThat(ficha.getEstadoFicha()).isEqualTo(estado);
        assertThat(ficha.getMotivoRevision()).isEqualTo("Motivo anterior");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void exigeMotivoAlRechazar(String motivo) throws Exception {
        Emprendedora ficha = crearFicha(EstadoFichaEmprendimiento.POR_REVISAR);
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .header("Authorization", tokenAdmin()).contentType("application/json")
                .content(json.writeValueAsString(new RevisionFichaRequest(Accion.RECHAZAR, motivo))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("MOTIVO_REQUERIDO"));
        assertThat(ficha.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.POR_REVISAR);
    }

    @Test
    void permiteLoginDespuesDeAprobarSinMotivo() throws Exception {
        Emprendedora ficha = crearFicha(EstadoFichaEmprendimiento.POR_REVISAR);
        String credenciales = "{\"email\":\"ana@example.cl\",\"password\":\"secreto\"}";
        mvc.perform(post("/auth/login").contentType("application/json").content(credenciales))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("CUENTA_POR_REVISAR"));
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .header("Authorization", tokenAdmin()).contentType("application/json").content("{\"accion\":\"APROBAR\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.motivoRevision").isEmpty());
        mvc.perform(post("/auth/login").contentType("application/json").content(credenciales))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
    }

    @Test
    void impideRevisionDeCuentaDadaDeBajaAunqueFichaSigaActiva() throws Exception {
        Emprendedora ficha = crearFicha(EstadoFichaEmprendimiento.ACTIVA);
        ficha.getUsuario().darDeBaja(Instant.now());
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .header("Authorization", tokenAdmin()).contentType("application/json").content("{\"accion\":\"OCULTAR\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaClienteYSolicitudSinToken() throws Exception {
        Emprendedora ficha = crearFicha(EstadoFichaEmprendimiento.POR_REVISAR);
        Usuario cliente = usuarios.save(new Usuario("Cliente", "cliente@example.cl", "hash", Rol.CLIENTE, true));
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .header("Authorization", "Bearer " + tokens.generar(cliente.getId(), Rol.CLIENTE))
                .contentType("application/json").content("{\"accion\":\"APROBAR\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", ficha.getId())
                .contentType("application/json").content("{\"accion\":\"APROBAR\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(ficha.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.POR_REVISAR);
    }

    @Test
    void devuelve404ParaFichaInexistente() throws Exception {
        mvc.perform(patch("/admin/emprendedoras/{id}/revision", Long.MAX_VALUE)
                .header("Authorization", tokenAdmin()).contentType("application/json").content("{\"accion\":\"APROBAR\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("EMPRENDEDORA_NO_ENCONTRADA"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"accion\":\"INVENTADA\"}"})
    void rechazaAccionInvalida(String cuerpo) throws Exception {
        mvc.perform(patch("/admin/emprendedoras/1/revision").header("Authorization", tokenAdmin())
                .contentType("application/json").content(cuerpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void flujoCompletoRegistroListadoAprobacionYAparicionPublica() throws Exception {
        RegistroEmprendedoraRequest reg = new RegistroEmprendedoraRequest(
                "Francisca Lagos", "francisca.lagos@example.cl", "secreto", true, "12.345.678-5",
                "Costuras Francisca", "Metropolitana", "Providencia",
                List.of(Rubro.REPARACION, Rubro.SUPRARRECICLAJE), "@costurasfrancisca"
        );
        mvc.perform(post("/auth/registro/emprendedora").contentType("application/json")
                .content(json.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoFicha").value("POR_REVISAR"));

        String tokenAdm = tokenAdmin();
        mvc.perform(get("/admin/emprendedoras?estado=POR_REVISAR").header("Authorization", tokenAdm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'francisca.lagos@example.cl')].nombreComercial").value("Costuras Francisca"));

        Emprendedora registrada = emprendedoras.listarParaAdmin(EstadoFichaEmprendimiento.POR_REVISAR).stream()
                .filter(e -> e.getUsuario().getEmail().equalsIgnoreCase("francisca.lagos@example.cl"))
                .findFirst().orElseThrow();

        mvc.perform(get("/emprendedoras?texto=Costuras").header("Authorization", tokenAdm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mvc.perform(patch("/admin/emprendedoras/{id}/revision", registrada.getId())
                .header("Authorization", tokenAdm)
                .contentType("application/json")
                .content("{\"accion\":\"APROBAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoFicha").value("ACTIVA"));

        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/emprendedoras?texto=Costuras").header("Authorization", tokenAdm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreComercial").value("Costuras Francisca"))
                .andExpect(jsonPath("$[0].id").value(registrada.getId()));

        String credenciales = "{\"email\":\"francisca.lagos@example.cl\",\"password\":\"secreto\"}";
        mvc.perform(post("/auth/login").contentType("application/json").content(credenciales))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.rol").value("EMPRENDEDORA"));
    }
}
