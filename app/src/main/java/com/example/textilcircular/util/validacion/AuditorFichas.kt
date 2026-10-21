package com.example.textilcircular.util.validacion

import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.ui.preview.DatosDemo

// Auditor de fichas de emprendimiento para identificar inconsistencias o datos faltantes.
object AuditorFichas {

    fun auditar(emprendedora: Emprendedora): List<String> {
        val problemas = mutableListOf<String>()

        val resRut = ValidadorRut.validar(emprendedora.rut)
        if (!resRut.esValido) {
            problemas.add("RUT no válido: ${resRut.mensajeError ?: "formato incorrecto"}")
        }

        val resNombre = ValidadorTexto.validarNombreTitular(emprendedora.nombre)
        if (!resNombre.esValido) {
            problemas.add(resNombre.mensajeError ?: "Nombre de titular incompleto")
        }

        val resNombreComercial = ValidadorTexto.validarLongitud(
            valor = emprendedora.nombreComercial,
            minimo = 3,
            maximo = 80,
            nombreCampo = "El nombre comercial"
        )
        if (!resNombreComercial.esValido) {
            problemas.add(resNombreComercial.mensajeError ?: "Nombre comercial incompleto")
        }

        val resEmail = ValidadorContacto.validarEmail(emprendedora.email)
        if (!resEmail.esValido) {
            problemas.add("Correo electrónico inválido: ${resEmail.mensajeError ?: "formato incorrecto"}")
        }

        if (!emprendedora.instagram.isNullOrBlank()) {
            val resIg = ValidadorContacto.validarInstagram(emprendedora.instagram)
            if (!resIg.esValido) {
                problemas.add(resIg.mensajeError ?: "Cuenta de Instagram con formato inválido")
            }
        }

        if (emprendedora.region.isBlank() || emprendedora.comuna.isBlank()) {
            problemas.add("Ubicación geográfica incompleta")
        } else {
            val comunasRegion = DatosDemo.RegionesConComunas[emprendedora.region]
            if (comunasRegion != null && !comunasRegion.any { it.equals(emprendedora.comuna, ignoreCase = true) }) {
                problemas.add("La comuna seleccionada no pertenece a la región indicada")
            }
        }

        if (emprendedora.rubros.isEmpty()) {
            problemas.add("No registra rubros productivos asignados")
        }

        return problemas
    }
}
