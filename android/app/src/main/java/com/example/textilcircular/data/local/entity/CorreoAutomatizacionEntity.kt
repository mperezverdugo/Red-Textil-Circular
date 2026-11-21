package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.RegistroAutomatizacionCorreo

// Entidad para registrar los envíos y reintentos de correos automáticos en el panel administrativo.
@Entity(tableName = "correos_automatizaciones")
data class CorreoAutomatizacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val destinatario: String,
    val emprendimiento: String,
    @ColumnInfo(name = "fecha_envio")
    val fechaEnvio: String,
    val estado: EstadoCorreo
) {
    // Convierte el registro al modelo usado por la pantalla de administración
    fun aModelo(): RegistroAutomatizacionCorreo {
        return RegistroAutomatizacionCorreo(
            id = id,
            destinatario = destinatario,
            emprendimiento = emprendimiento,
            fechaEnvio = fechaEnvio,
            estado = estado
        )
    }

    companion object {
        // Convierte el modelo de correo a entidad de Room
        fun desdeModelo(correo: RegistroAutomatizacionCorreo): CorreoAutomatizacionEntity {
            return CorreoAutomatizacionEntity(
                id = correo.id,
                destinatario = correo.destinatario,
                emprendimiento = correo.emprendimiento,
                fechaEnvio = correo.fechaEnvio,
                estado = correo.estado
            )
        }
    }
}
