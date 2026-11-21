package com.textilcircular.usuarios;

import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import com.textilcircular.usuarios.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(ProteccionRutasTest.RutasPrueba.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProteccionRutasTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired TokenService tokens;
    @Value("${usuarios.token.secreto}") String secreto;

    @RestController
    static class RutasPrueba {
        @GetMapping({"/prueba/protegida", "/admin/prueba"})
        String responder() {
            return "permitido";
        }
    }

    private Usuario crearUsuario(Rol rol) {
        return usuarios.save(new Usuario("Ana", "ana@example.cl", "hash", rol, true));
    }

    @Test
    void exigeTokenEnRutaProtegida() throws Exception {
        mvc.perform(get("/prueba/protegida")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"))
                .andExpect(jsonPath("$.mensaje").isString());
    }

    @Test
    void rechazaTokenAlterado() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        String token = tokens.generar(usuario.getId(), usuario.getRol());
        int posicion = token.lastIndexOf('.') + 1;
        char cambio = token.charAt(posicion) == 'A' ? 'B' : 'A';
        String alterado = token.substring(0, posicion) + cambio + token.substring(posicion + 1);
        mvc.perform(get("/prueba/protegida").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void rechazaTokenVencido() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        var emisor = new TokenService(secreto, Duration.ofMinutes(1),
                Clock.fixed(Instant.now().minusSeconds(3600), ZoneOffset.UTC));
        String token = emisor.generar(usuario.getId(), usuario.getRol());
        mvc.perform(get("/prueba/protegida").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void rechazaClienteEnAdministracion() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        mvc.perform(get("/admin/prueba").header("Authorization",
                "Bearer " + tokens.generar(usuario.getId(), usuario.getRol())))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void revocaTokenAlDarDeBajaLaCuenta() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        String token = tokens.generar(usuario.getId(), usuario.getRol());
        usuario.darDeBaja(Instant.now());
        mvc.perform(get("/prueba/protegida").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("CUENTA_DADA_DE_BAJA"));
    }

    @Test
    void permiteRegistroPublicoSinToken() throws Exception {
        mvc.perform(post("/auth/registro/cliente").contentType("application/json").content("""
                {"nombre":"Ana","email":"ana@example.cl","password":"secreto","aceptaLeyDatos":true}
                """)).andExpect(status().isCreated());
    }

    @Test
    void noHacePublicoGetAuth() throws Exception {
        mvc.perform(get("/auth/login")).andExpect(status().isUnauthorized());
    }

    @Test
    void noHacePublicaConsolaFueraDeDev() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Basic abc", "Bearer ", "Bearer no-es-un-token"})
    void rechazaEncabezadoInvalido(String encabezado) throws Exception {
        mvc.perform(get("/prueba/protegida").header("Authorization", encabezado))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void rechazaRolDistintoAlDeLaBase() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        mvc.perform(get("/admin/prueba").header("Authorization",
                "Bearer " + tokens.generar(usuario.getId(), Rol.ADMIN)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void permiteAdminYConservaValidacionDeRol() throws Exception {
        Usuario usuario = crearUsuario(Rol.ADMIN);
        String token = tokens.generar(usuario.getId(), Rol.ADMIN);
        assertThat(tokens.validar(token).rol()).isEqualTo(Rol.ADMIN);
        assertThat(tokens.validar(token, Rol.ADMIN).usuarioId()).isEqualTo(usuario.getId());
        mvc.perform(get("/admin/prueba").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().string("permitido"));
    }

    @Test
    void permiteClienteConTokenValido() throws Exception {
        Usuario usuario = crearUsuario(Rol.CLIENTE);
        mvc.perform(get("/prueba/protegida").header("Authorization",
                "Bearer " + tokens.generar(usuario.getId(), Rol.CLIENTE)))
                .andExpect(status().isOk()).andExpect(content().string("permitido"));
    }
}
