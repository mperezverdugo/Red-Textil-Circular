package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.textilcircular.model.Producto

// Entidad que representa la tabla de creaciones textiles de las emprendedoras en SQLite.
@Entity(
    tableName = "productos",
    foreignKeys = [
        ForeignKey(
            entity = EmprendedoraEntity::class,
            parentColumns = ["id"],
            childColumns = ["emprendedora_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["emprendedora_id"])
    ]
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "emprendedora_id")
    val emprendedoraId: Int,
    val nombre: String,
    @ColumnInfo(name = "descripcion_corta")
    val descripcionCorta: String,
    @ColumnInfo(name = "explicacion_proceso")
    val explicacionProceso: String? = null,
    val fotos: List<String> = emptyList(),
    @ColumnInfo(name = "pendiente_sincronizar")
    val pendienteSincronizar: Boolean = false
) {
    // Convierte el registro guardado en la base de datos a un Producto para la vista
    fun aModelo(): Producto {
        return Producto(
            id = id,
            emprendedoraId = emprendedoraId,
            nombre = nombre,
            descripcionCorta = descripcionCorta,
            explicacionProceso = explicacionProceso,
            fotos = fotos,
            pendienteSincronizar = pendienteSincronizar
        )
    }

    companion object {
        // Convierte el modelo de producto a entidad de Room
        fun desdeModelo(producto: Producto): ProductoEntity {
            return ProductoEntity(
                id = producto.id,
                emprendedoraId = producto.emprendedoraId,
                nombre = producto.nombre,
                descripcionCorta = producto.descripcionCorta,
                explicacionProceso = producto.explicacionProceso,
                fotos = producto.fotos,
                pendienteSincronizar = producto.pendienteSincronizar
            )
        }
    }
}
