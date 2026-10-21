package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Producto

// Entidad que representa la conversación única entre un cliente y una emprendedora.
@Entity(
    tableName = "conversaciones",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["cliente_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EmprendedoraEntity::class,
            parentColumns = ["id"],
            childColumns = ["emprendedora_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["producto_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["cliente_id", "emprendedora_id"], unique = true),
        Index(value = ["emprendedora_id"]),
        Index(value = ["producto_id"])
    ]
)
data class ConversacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "cliente_id")
    val clienteId: Int,
    @ColumnInfo(name = "emprendedora_id")
    val emprendedoraId: Int,
    @ColumnInfo(name = "producto_id")
    val productoId: Int? = null,
    @ColumnInfo(name = "nombre_cliente")
    val nombreCliente: String,
    @ColumnInfo(name = "nombre_emprendimiento")
    val nombreEmprendimiento: String = "",
    @ColumnInfo(name = "ultimo_mensaje")
    val ultimoMensaje: String,
    @ColumnInfo(name = "fecha_hora")
    val fechaHora: String,
    @ColumnInfo(name = "no_leidos_cliente")
    val noLeidosCliente: Int = 0,
    @ColumnInfo(name = "no_leidos_emprendedora")
    val noLeidosEmprendedora: Int = 0
) {
    // Convierte el registro a un modelo Conversacion
    fun aModelo(productoAsociado: Producto? = null): Conversacion {
        return Conversacion(
            id = id,
            emprendedoraId = emprendedoraId,
            clienteId = clienteId,
            nombreCliente = nombreCliente,
            nombreEmprendimiento = nombreEmprendimiento,
            ultimoMensaje = ultimoMensaje,
            fechaHora = fechaHora,
            noLeidosEmprendedora = noLeidosEmprendedora,
            noLeidosCliente = noLeidosCliente,
            productoAsociado = productoAsociado
        )
    }

    companion object {
        // Convierte el modelo a entidad
        fun desdeModelo(conversacion: Conversacion): ConversacionEntity {
            return ConversacionEntity(
                id = conversacion.id,
                clienteId = conversacion.clienteId,
                emprendedoraId = conversacion.emprendedoraId,
                productoId = conversacion.productoAsociado?.id,
                nombreCliente = conversacion.nombreCliente,
                nombreEmprendimiento = conversacion.nombreEmprendimiento,
                ultimoMensaje = conversacion.ultimoMensaje,
                fechaHora = conversacion.fechaHora,
                noLeidosCliente = conversacion.noLeidosCliente,
                noLeidosEmprendedora = conversacion.noLeidosEmprendedora
            )
        }
    }
}
