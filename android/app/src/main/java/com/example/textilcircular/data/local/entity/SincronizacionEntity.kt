package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.ItemSincronizacion

// Entidad para registrar elementos pendientes de respaldo en la base de datos local.
@Entity(tableName = "sincronizaciones_pendientes")
data class SincronizacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "tipo_elemento")
    val tipoElemento: String,
    val detalle: String,
    val estado: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE
) {
    // Convierte el registro guardado al modelo de la vista
    fun aModelo(): ItemSincronizacion {
        return ItemSincronizacion(
            id = id,
            tipoElemento = tipoElemento,
            detalle = detalle,
            estado = estado
        )
    }

    companion object {
        // Convierte el modelo a entidad
        fun desdeModelo(item: ItemSincronizacion): SincronizacionEntity {
            return SincronizacionEntity(
                id = item.id,
                tipoElemento = item.tipoElemento,
                detalle = item.detalle,
                estado = item.estado
            )
        }
    }
}
