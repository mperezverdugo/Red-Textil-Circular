package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.TipoEmisor

// Entidad que representa un mensaje individual dentro de una conversación.
@Entity(
    tableName = "mensajes",
    foreignKeys = [
        ForeignKey(
            entity = ConversacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversacion_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversacion_id"])
    ]
)
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "conversacion_id")
    val conversacionId: Int,
    val emisor: TipoEmisor,
    val contenido: String,
    @ColumnInfo(name = "fecha_hora")
    val fechaHora: String,
    @ColumnInfo(name = "es_leido")
    val esLeido: Boolean = true,
    @ColumnInfo(name = "producto_id")
    val productoId: Int? = null,
    @ColumnInfo(name = "nombre_producto")
    val nombreProducto: String? = null
) {
    // Convierte el registro guardado en la base de datos a un Mensaje para el chat
    fun aModelo(): Mensaje {
        return Mensaje(
            id = id,
            conversacionId = conversacionId,
            emisor = emisor,
            contenido = contenido,
            fechaHora = fechaHora,
            esLeido = esLeido,
            productoId = productoId,
            nombreProducto = nombreProducto
        )
    }

    companion object {
        // Convierte el modelo de mensaje a entidad de Room
        fun desdeModelo(mensaje: Mensaje): MensajeEntity {
            return MensajeEntity(
                id = mensaje.id,
                conversacionId = mensaje.conversacionId,
                emisor = mensaje.emisor,
                contenido = mensaje.contenido,
                fechaHora = mensaje.fechaHora,
                esLeido = mensaje.esLeido,
                productoId = mensaje.productoId,
                nombreProducto = mensaje.nombreProducto
            )
        }
    }
}
