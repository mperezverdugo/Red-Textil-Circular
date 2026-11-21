package com.example.textilcircular.util.validacion

// Representa el resultado de una regla de validación con su mensaje en caso de error.
data class ResultadoValidacion(
    val esValido: Boolean,
    val mensajeError: String? = null
) {
    companion object {
        fun ok(): ResultadoValidacion = ResultadoValidacion(esValido = true)

        fun error(mensaje: String): ResultadoValidacion = ResultadoValidacion(
            esValido = false,
            mensajeError = mensaje
        )
    }
}
