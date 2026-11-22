package com.textilcircular.mercado;

import com.textilcircular.mercado.model.Rol;
import com.textilcircular.mercado.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(ProteccionRutasTest.RutasPrueba.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProteccionRutasTest {
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Value("${mercado.token.secreto}") String secreto;

    @RestController
    static class RutasPrueba {
        @GetMapping("/prueba/protegida")
        String responder() {
            return "permitido";
        }
    }

    @Test
    void exigeTokenEnRutaProtegida() throws Exception {
        mvc.perform(get("/prueba/protegida")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"))
                .andExpect(jsonPath("$.mensaje").isString());
    }

    @Test
    void rechazaTokenAlterado() throws Exception {
        String token = tokens.generar(2L, Rol.CLIENTE);
        int posicion = token.lastIndexOf('.') + 1;
        char cambio = token.charAt(posicion) == 'A' ? 'B' : 'A';
        String alterado = token.substring(0, posicion) + cambio + token.substring(posicion + 1);
        mvc.perform(get("/prueba/protegida").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void rechazaTokenVencido() throws Exception {
        var emisor = new TokenService(secreto, Duration.ofMinutes(1),
                Clock.fixed(Instant.now().minusSeconds(3600), ZoneOffset.UTC));
        String token = emisor.generar(2L, Rol.CLIENTE);
        mvc.perform(get("/prueba/protegida").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Basic abc", "Bearer ", "Bearer no-es-un-token"})
    void rechazaEncabezadoInvalido(String encabezado) throws Exception {
        mvc.perform(get("/prueba/protegida").header("Authorization", encabezado))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void permiteTokenValido() throws Exception {
        String token = tokens.generar(2L, Rol.EMPRENDEDORA);
        mvc.perform(get("/prueba/protegida").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().string("permitido"));
    }
}
