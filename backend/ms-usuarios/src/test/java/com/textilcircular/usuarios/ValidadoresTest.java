package com.textilcircular.usuarios;

import com.textilcircular.usuarios.service.Validadores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class ValidadoresTest {
    @ParameterizedTest
    @ValueSource(strings = {"12.345.678-5", "123456785", "6.000.000-k", "10000004-0", " 12.345.678-5 "})
    void aceptaRut(String rut) {
        assertThatCode(() -> Validadores.validarRut(rut)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "123", "1234567890", "12345678-9", "0000000-0", "abcdefg-k", "12345678-X"})
    void rechazaRut(String rut) {
        assertThatThrownBy(() -> Validadores.validarRut(rut)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@example.cl", " ana+uno@example.cl ", "A.B_C-D@sub.example.com"})
    void aceptaCorreo(String correo) {
        assertThatCode(() -> Validadores.validarCorreo(correo)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "ana", "ana@", "@example.cl", "ana@example.c", "a b@example.cl"})
    void rechazaCorreo(String correo) {
        assertThatThrownBy(() -> Validadores.validarCorreo(correo)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdef", "123456", " a b c ", "contraseña extensa"})
    void aceptaContrasena(String password) {
        assertThatCode(() -> Validadores.validarContrasena(password)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "      ", "12345"})
    void rechazaContrasena(String password) {
        assertThatThrownBy(() -> Validadores.validarContrasena(password)).isInstanceOf(IllegalArgumentException.class);
    }
}
