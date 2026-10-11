package com.example.textilcircular.util.validacion

import com.example.textilcircular.model.Emprendedora

// Auditor de fichas de emprendimiento para identificar inconsistencias o datos faltantes.
object AuditorFichas {

    fun auditar(emprendedora: Emprendedora): List<String> {
        val problemas = mutableListOf<String>()

        val resRut = ValidadorRut.validar(emprendedora.rut)
        if (!resRut.esValido) {
            problemas.add("RUT no válido (${resRut.mensajeError ?: "formato incorrecto"})")
        }

        if (emprendedora.nombre.trim().length < 3) {
            problemas.add("Nombre de titular incompleto")
        }

        if (emprendedora.nombreComercial.trim().length < 3) {
            problemas.add("Nombre comercial incompleto")
        }

        val resEmail = ValidadorContacto.validarEmail(emprendedora.email)
        if (!resEmail.esValido) {
            problemas.add("Correo electrónico inválido")
        }

        if (!emprendedora.instagram.isNullOrBlank()) {
            val resIg = ValidadorContacto.validarInstagram(emprendedora.instagram)
            if (!resIg.esValido) {
                problemas.add("Cuenta de Instagram con formato inválido")
            }
        }

        if (emprendedora.region.isBlank() || emprendedora.comuna.isBlank()) {
            problemas.add("Ubicación geográfica incompleta")
        }

        if (emprendedora.rubros.isEmpty()) {
            problemas.add("No registra rubros productivos asignados")
        }

        if (!emprendedora.activa) {
            problemas.add("Ficha actualmente inactiva")
        }

        return problemas
    }
}
