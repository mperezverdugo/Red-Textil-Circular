package com.example.textilcircular.util.validacion

// Validador de mensajes de chat entre clientes y emprendedoras.
object ValidadorChat {

    const val MAX_CARACTERES_MENSAJE = 1000

    fun validarMensaje(texto: String): ResultadoValidacion {
        val limpio = texto.trim()
        return when {
            limpio.isBlank() -> {
                ResultadoValidacion.error("El mensaje no puede estar vacío")
            }
            limpio.length > MAX_CARACTERES_MENSAJE -> {
                ResultadoValidacion.error("El mensaje supera el límite de $MAX_CARACTERES_MENSAJE caracteres")
            }
            else -> ResultadoValidacion.ok()
        }
    }
}
