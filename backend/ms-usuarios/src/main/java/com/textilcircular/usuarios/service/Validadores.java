package com.textilcircular.usuarios.service;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Validadores {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validadores() {}

    public static void validarCorreo(String email) {
        String limpio = email == null ? "" : email.strip();
        if (limpio.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }
        if (!EMAIL.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido");
        }
    }

    public static void validarContrasena(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    public static String limpiarRut(String rut) {
        return rut == null ? "" : rut.replace(".", "").replace("-", "").strip().toUpperCase(Locale.ROOT);
    }

    public static char calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - suma % 11;
        return resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
    }

    public static void validarRut(String rut) {
        String limpio = limpiarRut(rut);
        if (limpio.isBlank()) {
            throw new IllegalArgumentException("El RUT no puede estar vacío");
        }
        if (limpio.length() < 8 || limpio.length() > 9) {
            throw new IllegalArgumentException("El RUT debe tener entre 7 y 8 dígitos más el dígito verificador");
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        if (!cuerpo.chars().allMatch(c -> Character.isDigit((char) c))) {
            throw new IllegalArgumentException("El cuerpo del RUT solo debe contener números");
        }
        if (Long.parseLong(cuerpo) <= 0) {
            throw new IllegalArgumentException("El número de RUT no es válido");
        }
        char dv = limpio.charAt(limpio.length() - 1);
        if ((dv < '0' || dv > '9') && dv != 'K') {
            throw new IllegalArgumentException("El dígito verificador debe ser un número o la letra K");
        }
        if (dv != calcularDigitoVerificador(cuerpo)) {
            throw new IllegalArgumentException("RUT inválido");
        }
    }
}
