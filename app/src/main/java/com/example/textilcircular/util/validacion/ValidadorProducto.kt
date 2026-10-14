package com.example.textilcircular.util.validacion

import com.example.textilcircular.util.Constantes

// Validador de los campos del formulario de productos del catálogo.
object ValidadorProducto {

    const val MIN_CARACTERES_NOMBRE = 3
    const val MAX_CARACTERES_NOMBRE = 80
    const val MIN_CARACTERES_DESCRIPCION = 10
    const val MAX_CARACTERES_DESCRIPCION = 250
    const val MAX_CARACTERES_PROCESO = 500

    fun validarNombre(nombre: String): ResultadoValidacion {
        val limpio = nombre.trim()
        return when {
            limpio.isBlank() -> {
                ResultadoValidacion.error("El nombre del producto no puede estar vacío")
            }
            limpio.length < MIN_CARACTERES_NOMBRE -> {
                ResultadoValidacion.error("El nombre debe tener al menos $MIN_CARACTERES_NOMBRE caracteres")
            }
            limpio.length > MAX_CARACTERES_NOMBRE -> {
                ResultadoValidacion.error("El nombre no puede superar los $MAX_CARACTERES_NOMBRE caracteres")
            }
            else -> ResultadoValidacion.ok()
        }
    }

    fun validarDescripcion(descripcion: String): ResultadoValidacion {
        val limpio = descripcion.trim()
        return when {
            limpio.isBlank() -> {
                ResultadoValidacion.error("La descripción corta no puede estar vacía")
            }
            limpio.length < MIN_CARACTERES_DESCRIPCION -> {
                ResultadoValidacion.error("La descripción debe tener al menos $MIN_CARACTERES_DESCRIPCION caracteres")
            }
            limpio.length > MAX_CARACTERES_DESCRIPCION -> {
                ResultadoValidacion.error("La descripción no puede superar los $MAX_CARACTERES_DESCRIPCION caracteres")
            }
            else -> ResultadoValidacion.ok()
        }
    }

    fun validarExplicacionProceso(proceso: String?): ResultadoValidacion {
        if (proceso.isNullOrBlank()) return ResultadoValidacion.ok()
        val limpio = proceso.trim()
        return if (limpio.length > MAX_CARACTERES_PROCESO) {
            ResultadoValidacion.error("La explicación del proceso no puede superar los $MAX_CARACTERES_PROCESO caracteres")
        } else {
            ResultadoValidacion.ok()
        }
    }

    fun validarFotos(fotos: List<String>, obligatoriaAlMenosUna: Boolean = false): ResultadoValidacion {
        return when {
            obligatoriaAlMenosUna && fotos.isEmpty() -> {
                ResultadoValidacion.error("Debes agregar al menos una foto del producto")
            }
            fotos.size > Constantes.MAX_FOTOS_POR_PRODUCTO -> {
                ResultadoValidacion.error("No puedes agregar más de ${Constantes.MAX_FOTOS_POR_PRODUCTO} fotos")
            }
            else -> ResultadoValidacion.ok()
        }
    }

    fun validarFormularioCompleto(
        nombre: String,
        descripcion: String,
        explicacionProceso: String? = null,
        fotos: List<String> = emptyList(),
        obligatoriaAlMenosUnaFoto: Boolean = false
    ): ResultadoValidacion {
        val resNombre = validarNombre(nombre)
        if (!resNombre.esValido) return resNombre

        val resDesc = validarDescripcion(descripcion)
        if (!resDesc.esValido) return resDesc

        val resProceso = validarExplicacionProceso(explicacionProceso)
        if (!resProceso.esValido) return resProceso

        val resFotos = validarFotos(fotos, obligatoriaAlMenosUnaFoto)
        if (!resFotos.esValido) return resFotos

        return ResultadoValidacion.ok()
    }
}
