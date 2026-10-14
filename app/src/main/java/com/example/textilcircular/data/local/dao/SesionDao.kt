package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import kotlinx.coroutines.flow.Flow

// Operaciones para persistir la sesión activa en SQLite y restaurarla al abrir la app.
@Dao
interface SesionDao {

    // Emite la sesión guardada actualmente
    @Query("SELECT * FROM sesion_activa WHERE id = 1 LIMIT 1")
    fun obtenerSesion(): Flow<SesionActivaEntity?>

    // Lectura directa de la sesión activa
    @Query("SELECT * FROM sesion_activa WHERE id = 1 LIMIT 1")
    suspend fun obtenerSesionDirecta(): SesionActivaEntity?

    // Guarda o reemplaza la sesión activa única
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(sesion: SesionActivaEntity)

    // Borra la sesión cuando el usuario decide salir
    @Query("DELETE FROM sesion_activa WHERE id = 1")
    suspend fun cerrarSesion(): Int
}
