package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.textilcircular.model.Cliente

// Entidad que representa la tabla de clientes en SQLite para consultas y autenticación.
@Entity(
    tableName = "clientes",
    indices = [Index(value = ["email"], unique = true)]
)
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val email: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    val sal: String,
    @ColumnInfo(name = "acepta_ley_datos")
    val aceptaLeyDatos: Boolean = true
) {
    // Convierte el registro guardado en la base de datos al modelo del dominio
    fun aModelo(): Cliente {
        return Cliente(
            id = id,
            nombre = nombre,
            email = email,
            passwordHash = passwordHash,
            sal = sal,
            aceptaLeyDatos = aceptaLeyDatos
        )
    }

    companion object {
        // Convierte el modelo de cliente a entidad de Room
        fun desdeModelo(cliente: Cliente): ClienteEntity {
            return ClienteEntity(
                id = cliente.id,
                nombre = cliente.nombre,
                email = cliente.email,
                passwordHash = cliente.passwordHash,
                sal = cliente.sal,
                aceptaLeyDatos = cliente.aceptaLeyDatos
            )
        }
    }
}
