package com.textilcircular.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.dto.RegistroClienteRequest;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistroClienteTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    @Test
    void registraClienteConHashSinExponerCredenciales() throws Exception {
        var solicitud = new RegistroClienteRequest("Ana", " ANA@example.cl ", "secreto", true);
        mvc.perform(post("/auth/registro/cliente").contentType("application/json")
                .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.email").value("ana@example.cl"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        var usuario = usuarios.findByEmail("ana@example.cl").orElseThrow();
        assertThat(usuario.getPasswordHash()).startsWith("$2");
        assertThat(encoder.matches("secreto", usuario.getPasswordHash())).isTrue();
    }

    @Test
    void rechazaCorreoDuplicado() throws Exception {
        String cuerpo = json.writeValueAsString(new RegistroClienteRequest("Ana", "ana@example.cl", "secreto", true));
        mvc.perform(post("/auth/registro/cliente").contentType("application/json").content(cuerpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/auth/registro/cliente").contentType("application/json").content(cuerpo))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CORREO_DUPLICADO"));
    }

    static Stream<RegistroClienteRequest> datosInvalidos() {
        return Stream.of(
                new RegistroClienteRequest("", "a@example.cl", "secreto", true),
                new RegistroClienteRequest("Ana", "invalido", "secreto", true),
                new RegistroClienteRequest("Ana", "a@example.cl", "12345", true),
                new RegistroClienteRequest("Ana", "a@example.cl", null, true),
                new RegistroClienteRequest("Ana", "a@example.cl", "secreto", false));
    }

    @ParameterizedTest
    @MethodSource("datosInvalidos")
    void rechazaDatosInvalidos(RegistroClienteRequest solicitud) throws Exception {
        mvc.perform(post("/auth/registro/cliente").contentType("application/json")
                .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        assertThat(usuarios.findByEmail("a@example.cl")).isEmpty();
    }
}
