package com.textilcircular.usuarios;

import com.textilcircular.usuarios.model.Rol;
import com.textilcircular.usuarios.security.TokenService;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

class TokenServiceTest {
    private static final String SECRETO = "clave-exclusiva-para-pruebas-textil-circular-2026";
    private static final Instant AHORA = Instant.parse("2026-11-21T12:00:00Z");

    private TokenService servicio(Instant fecha) {
        return new TokenService(SECRETO, Duration.ofHours(1), Clock.fixed(fecha, ZoneOffset.UTC));
    }

    @Test
    void validaTokenYRecuperaIdYRol() {
        TokenService servicio = servicio(AHORA);
        String token = servicio.generar(7L, Rol.CLIENTE);
        assertThat(servicio.validar(token, Rol.CLIENTE))
                .isEqualTo(new TokenService.DatosToken(7L, Rol.CLIENTE));
    }

    @Test
    void rechazaTokenVencido() {
        String token = servicio(AHORA).generar(7L, Rol.CLIENTE);
        assertThatThrownBy(() -> servicio(AHORA.plusSeconds(3600)).validar(token, Rol.CLIENTE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaFirmaAlterada() {
        TokenService servicio = servicio(AHORA);
        String token = servicio.generar(7L, Rol.CLIENTE);
        int posicion = token.lastIndexOf('.') + 1;
        char cambio = token.charAt(posicion) == 'A' ? 'B' : 'A';
        String alterado = token.substring(0, posicion) + cambio + token.substring(posicion + 1);
        assertThatThrownBy(() -> servicio.validar(alterado, Rol.CLIENTE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaRolIncorrectoAunqueLaFirmaSeaValida() {
        TokenService servicio = servicio(AHORA);
        String token = servicio.generar(7L, Rol.CLIENTE);
        assertThatThrownBy(() -> servicio.validar(token, Rol.ADMIN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaTokenConOtraClave() {
        String token = servicio(AHORA).generar(7L, Rol.CLIENTE);
        var otro = new TokenService("otra-clave-para-pruebas-textil-circular-2026",
                Duration.ofHours(1), Clock.fixed(AHORA, ZoneOffset.UTC));
        assertThatThrownBy(() -> otro.validar(token, Rol.CLIENTE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaSecretoCorto() {
        assertThatThrownBy(() -> new TokenService("corto", Duration.ofHours(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
