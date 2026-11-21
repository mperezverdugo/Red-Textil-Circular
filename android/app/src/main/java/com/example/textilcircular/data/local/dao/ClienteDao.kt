package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.textilcircular.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

// Consultas y operaciones para la tabla de clientes en SQLite.
@Dao
interface ClienteDao {

    // Retorna todos los clientes registrados
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<ClienteEntity>>

    // Busca un cliente por su id de forma reactiva
    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): Flow<ClienteEntity?>

    // Busca un cliente por su id para operaciones puntuales
    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorIdDirecto(id: Int): ClienteEntity?

    // Busca un cliente por su correo normalizado
    @Query("SELECT * FROM clientes WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun obtenerPorEmail(email: String): Flow<ClienteEntity?>

    // Busca un cliente por su correo para autenticación
    @Query("SELECT * FROM clientes WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun obtenerPorEmailDirecto(email: String): ClienteEntity?

    // Registra un nuevo cliente
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(cliente: ClienteEntity): Long

    // Inserta clientes iniciales en lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(clientes: List<ClienteEntity>)

    // Actualiza datos de un cliente
    @Update
    suspend fun actualizar(cliente: ClienteEntity): Int
}
