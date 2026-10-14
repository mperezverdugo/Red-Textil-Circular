package com.example.textilcircular.util.validacion

// Validador de vías de contacto: correo electrónico, cuenta de Instagram y teléfono.
object ValidadorContacto {

    private val PATRON_EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PATRON_INSTAGRAM = Regex("^[a-zA-Z0-9._]{1,30}$")
    private val PATRON_TELEFONO = Regex("^\\+?[0-9]{8,15}$")

    fun validarEmail(email: String): ResultadoValidacion {
        val limpio = email.trim()
        return when {
            limpio.isBlank() -> {
                ResultadoValidacion.error("El correo electrónico no puede estar vacío")
            }
            !PATRON_EMAIL.matches(limpio) -> {
                ResultadoValidacion.error("El formato del correo electrónico no es válido")
            }
            else -> ResultadoValidacion.ok()
        }
    }

    fun limpiarUsuarioInstagram(usuario: String): String {
        return usuario.trim().removePrefix("@")
    }

    fun validarInstagram(usuario: String?, obligatorio: Boolean = false): ResultadoValidacion {
        if (usuario.isNullOrBlank()) {
            return if (obligatorio) {
                ResultadoValidacion.error("El usuario de Instagram es obligatorio")
            } else {
                ResultadoValidacion.ok()
            }
        }

        val limpio = limpiarUsuarioInstagram(usuario)
        return when {
            limpio.length > 30 -> {
                ResultadoValidacion.error("El usuario de Instagram no puede superar los 30 caracteres")
            }
            !PATRON_INSTAGRAM.matches(limpio) -> {
                ResultadoValidacion.error("El usuario de Instagram solo puede contener letras, números, puntos y guiones bajos")
            }
            else -> ResultadoValidacion.ok()
        }
    }

    fun validarTelefono(telefono: String?, obligatorio: Boolean = false): ResultadoValidacion {
        if (telefono.isNullOrBlank()) {
            return if (obligatorio) {
                ResultadoValidacion.error("El teléfono es obligatorio")
            } else {
                ResultadoValidacion.ok()
            }
        }

        val limpio = telefono.replace(" ", "").replace("-", "").trim()
        return if (PATRON_TELEFONO.matches(limpio)) {
            ResultadoValidacion.ok()
        } else {
            ResultadoValidacion.error("El formato de teléfono no es válido")
        }
    }
}
