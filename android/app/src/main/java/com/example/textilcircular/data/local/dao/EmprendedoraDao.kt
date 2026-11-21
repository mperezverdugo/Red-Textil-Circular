package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import kotlinx.coroutines.flow.Flow

// Consultas y operaciones para la tabla de emprendedoras en SQLite.
@Dao
interface EmprendedoraDao {

    // Retorna todas las emprendedoras para el directorio completo
    @Query("SELECT * FROM emprendedoras ORDER BY LOWER(nombre_comercial) ASC")
    fun obtenerTodas(): Flow<List<EmprendedoraEntity>>

    // Busca una emprendedora específica por su identificador
    @Query("SELECT * FROM emprendedoras WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): Flow<EmprendedoraEntity?>

    // Busca una emprendedora de forma directa para operaciones internas del repositorio
    @Query("SELECT * FROM emprendedoras WHERE id = :id LIMIT 1")
    suspend fun obtenerPorIdDirecto(id: Int): EmprendedoraEntity?

    // Busca una emprendedora por su correo electrónico para autenticación segura
    @Query("SELECT * FROM emprendedoras WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun obtenerPorEmailDirecto(email: String): EmprendedoraEntity?

    // Observa una emprendedora según su correo electrónico
    @Query("SELECT * FROM emprendedoras WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun obtenerPorEmail(email: String): Flow<EmprendedoraEntity?>

    // Busca una emprendedora por su RUT para evitar duplicados
    @Query("SELECT * FROM emprendedoras WHERE rut = :rut LIMIT 1")
    suspend fun obtenerPorRutDirecto(rut: String): EmprendedoraEntity?

    // Filtra las artesanas activas en el mercado según texto, región y comuna
    @Query("""
        SELECT * FROM emprendedoras 
        WHERE activa = 1 
          AND (:busqueda = '' OR LOWER(nombre_comercial) LIKE '%' || :busqueda || '%' 
                              OR LOWER(comuna) LIKE '%' || :busqueda || '%' 
                              OR LOWER(region) LIKE '%' || :busqueda || '%' 
                              OR LOWER(rubros) LIKE '%' || :busqueda || '%')
          AND (:region IS NULL OR :region = '' OR LOWER(region) = LOWER(:region))
          AND (:comuna IS NULL OR :comuna = '' OR LOWER(comuna) = LOWER(:comuna))
        ORDER BY LOWER(nombre_comercial) ASC
    """)
    fun obtenerMercado(
        busqueda: String = "",
        region: String? = null,
        comuna: String? = null
    ): Flow<List<EmprendedoraEntity>>

    // Inserta o reemplaza una emprendedora registrada
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(emprendedora: EmprendedoraEntity): Long

    // Inserta una lista completa (útil para la carga de datos iniciales)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarias(emprendedoras: List<EmprendedoraEntity>)

    // Actualiza los datos de la ficha
    @Update
    suspend fun actualizar(emprendedora: EmprendedoraEntity): Int

    // Cambia la visibilidad en el mercado (activa o inactiva)
    @Query("""
        UPDATE emprendedoras 
        SET activa = :activa, 
            estado_ficha = CASE WHEN :activa = 1 THEN 'ACTIVA' ELSE 'OCULTA' END 
        WHERE id = :id
    """)
    suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Int

    // Actualiza el estado de moderación administrativa
    @Query("UPDATE emprendedoras SET estado_ficha = :estado, activa = :activa WHERE id = :id")
    suspend fun cambiarEstadoFicha(id: Int, estado: EstadoFichaEmprendimiento, activa: Boolean): Int

    // Elimina el registro por solicitud de baja definitiva
    @Query("DELETE FROM emprendedoras WHERE id = :id")
    suspend fun eliminarPorId(id: Int): Int
}
