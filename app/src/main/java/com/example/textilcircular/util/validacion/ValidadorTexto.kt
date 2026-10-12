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

    fun validarNombreTitular(nombre: String): ResultadoValidacion {
        val limpio = limpiar(nombre)
        if (limpio.isBlank()) {
            return ResultadoValidacion.error("El nombre del titular no puede estar vacío")
        }
        if (limpio.length < 3) {
            return ResultadoValidacion.error("El nombre del titular debe tener al menos 3 caracteres")
        }
        val palabras = limpio.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (palabras.size < 2) {
            return ResultadoValidacion.error("El titular debe incluir al menos nombre y apellido")
        }
        val soloLetras = limpio.all { it.isLetter() || it.isWhitespace() }
        if (!soloLetras) {
            return ResultadoValidacion.error("El nombre del titular solo puede contener letras")
        }
        return ResultadoValidacion.ok()
    }
}
