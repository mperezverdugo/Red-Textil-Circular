package com.example.textilcircular.data.remote.dto

import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.util.validacion.AuditorFichas

// DTO administrativo de emprendedora devuelto por ms-usuarios (/admin/emprendedoras).
data class EmprendedoraAdminDto(
    val id: Long,
    val usuarioId: Long,
    val nombre: String,
    val email: String,
    val rut: String,
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<String>,
    val instagram: String? = null,
    val activa: Boolean = true,
    val estadoFicha: String,
    val motivoRevision: String? = null,
    val fechaRevision: String? = null
) {
    fun aEstadoFicha(): EstadoFichaEmprendimiento {
        return try {
            EstadoFichaEmprendimiento.valueOf(estadoFicha)
        } catch (_: Exception) {
            EstadoFichaEmprendimiento.POR_REVISAR
        }
    }

    fun aModelo(): Emprendedora {
        val estado = aEstadoFicha()
        return Emprendedora(
            id = id.toInt(),
            rut = rut,
            nombre = nombre,
            nombreComercial = nombreComercial,
            region = region,
            comuna = comuna,
            rubros = rubros.mapNotNull { nombreRubro ->
                try { Rubro.valueOf(nombreRubro) } catch (_: Exception) { null }
            },
            email = email,
            instagram = instagram,
            activa = activa,
            usuarioId = usuarioId.toInt(),
            estadoFicha = estado
        )
    }

    fun aFichaAdmin(): FichaAdminEmprendimiento {
        val emp = aModelo()
        return FichaAdminEmprendimiento(
            emprendedora = emp,
            estadoFicha = aEstadoFicha(),
            problemasDetectados = AuditorFichas.auditar(emp)
        )
    }
}
