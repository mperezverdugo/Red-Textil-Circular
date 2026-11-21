package com.example.textilcircular.util.validacion

// Validador de contraseñas y confirmación de seguridad para autenticación.
object ValidadorContrasena {

    const val LONGITUD_MINIMA_POR_DEFECTO = 6

    fun validar(password: String, longitudMinima: Int = LONGITUD_MINIMA_POR_DEFECTO): ResultadoValidacion {
        return when {
            password.isBlank() -> {
                ResultadoValidacion.error("La contraseña no puede estar vacía")
            }
            password.length < longitudMinima -> {
                ResultadoValidacion.error("La contraseña debe tener al menos $longitudMinima caracteres")
            }
            else -> ResultadoValidacion.ok()
        }
    }

    fun validarCoincidencia(password: String, confirmacion: String): ResultadoValidacion {
        val validacionBase = validar(password)
        if (!validacionBase.esValido) {
            return validacionBase
        }
        return if (password == confirmacion) {
            ResultadoValidacion.ok()
        } else {
            ResultadoValidacion.error("Las contraseñas no coinciden")
        }
    }
}
