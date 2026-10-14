package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.textilcircular.data.local.entity.SincronizacionEntity
import kotlinx.coroutines.flow.Flow

// Consultas y operaciones para la cola de sincronización local en SQLite.
@Dao
interface SincronizacionDao {

    // Lista de cambios pendientes de respaldo
    @Query("SELECT * FROM sincronizaciones_pendientes ORDER BY id ASC")
    fun obtenerPendientes(): Flow<List<SincronizacionEntity>>

    // Registra un nuevo elemento pendiente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: SincronizacionEntity): Long

    // Carga inicial de pendientes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(items: List<SincronizacionEntity>)

    // Limpia la cola cuando la sincronización fue completada con éxito
    @Query("DELETE FROM sincronizaciones_pendientes")
    suspend fun eliminarTodos(): Int

    // Cuenta cuántos elementos quedan pendientes
    @Query("SELECT COUNT(*) FROM sincronizaciones_pendientes")
    suspend fun contarPendientes(): Int
}
