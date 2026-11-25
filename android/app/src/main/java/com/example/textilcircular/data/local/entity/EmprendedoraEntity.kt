package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.Rubro

// Entidad que representa la tabla de emprendedoras registradas en SQLite.
@Entity(
    tableName = "emprendedoras",
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["email"], unique = false)
    ]
)
data class EmprendedoraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rut: String,
    val nombre: String,
    @ColumnInfo(name = "nombre_comercial")
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<Rubro>,
    val email: String,
    val instagram: String? = null,
    val activa: Boolean = true,
    @ColumnInfo(name = "estado_ficha")
    val estadoFicha: EstadoFichaEmprendimiento = EstadoFichaEmprendimiento.ACTIVA,
    @ColumnInfo(name = "problemas_detectados")
    val problemasDetectados: List<String> = emptyList(),
    @ColumnInfo(name = "usuario_id")
    val usuarioId: Int = 0,
    @ColumnInfo(name = "motivo_revision")
    val motivoRevision: String? = null
) {
    // Convierte la entidad de la base de datos al modelo que usa la interfaz de usuario
    fun aModelo(): Emprendedora {
        return Emprendedora(
            id = id,
            rut = rut,
            nombre = nombre,
            nombreComercial = nombreComercial,
            region = region,
            comuna = comuna,
            rubros = rubros,
            email = email,
            instagram = instagram,
            activa = activa,
            usuarioId = if (usuarioId > 0) usuarioId else id,
            estadoFicha = estadoFicha
        )
    }

    // Convierte hacia el modelo de ficha administrativa
    fun aFichaAdmin(): FichaAdminEmprendimiento {
        return FichaAdminEmprendimiento(
            emprendedora = aModelo(),
            estadoFicha = estadoFicha,
            problemasDetectados = problemasDetectados
        )
    }

    companion object {
        // Crea una entidad a partir del modelo de la aplicación
        fun desdeModelo(
            emprendedora: Emprendedora,
            estadoFicha: EstadoFichaEmprendimiento = emprendedora.estadoFicha,
            problemas: List<String> = emptyList(),
            motivoRevision: String? = null
        ): EmprendedoraEntity {
            return EmprendedoraEntity(
                id = emprendedora.id,
                rut = emprendedora.rut,
                nombre = emprendedora.nombre,
                nombreComercial = emprendedora.nombreComercial,
                region = emprendedora.region,
                comuna = emprendedora.comuna,
                rubros = emprendedora.rubros,
                email = emprendedora.email,
                instagram = emprendedora.instagram,
                activa = emprendedora.activa,
                estadoFicha = estadoFicha,
                problemasDetectados = problemas,
                usuarioId = emprendedora.usuarioId,
                motivoRevision = motivoRevision
            )
        }
    }
}
