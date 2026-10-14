package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.textilcircular.model.Rol

// Entidad de fila única que guarda la sesión activa para que el usuario no tenga que iniciar sesión cada vez.
@Entity(tableName = "sesion_activa")
data class SesionActivaEntity(
    @PrimaryKey
    val id: Int = 1,
    val rol: Rol,
    @ColumnInfo(name = "usuario_id")
    val usuarioId: Int? = null,
    val correo: String = ""
)
