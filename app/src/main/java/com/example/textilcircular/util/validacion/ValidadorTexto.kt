package com.example.textilcircular.util.validacion

// Funciones utilitarias para validar textos y campos de formulario generales.
object ValidadorTexto {

    fun limpiar(valor: String): String = valor.trim()

    fun validarNoVacio(valor: String, nombreCampo: String = "El campo"): ResultadoValidacion {
        val limpio = limpiar(valor)
        return if (limpio.isBlank()) {
            ResultadoValidacion.error("$nombreCampo no puede estar vacío")
        } else {
            ResultadoValidacion.ok()
        }
    }

    fun validarLongitud(
        valor: String,
        minimo: Int,
        maximo: Int,
        nombreCampo: String = "El campo"
    ): ResultadoValidacion {
        val limpio = limpiar(valor)
        return when {
            limpio.length < minimo -> {
                ResultadoValidacion.error("$nombreCampo debe tener al menos $minimo caracteres")
            }
            limpio.length > maximo -> {
                ResultadoValidacion.error("$nombreCampo no puede superar los $maximo caracteres")
            }
            else -> ResultadoValidacion.ok()
        }
    }
}
