package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.textilcircular.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

// Consultas y operaciones para el catálogo de productos en SQLite.
@Dao
interface ProductoDao {

    // Retorna todos los productos creados por una emprendedora específica
    @Query("SELECT * FROM productos WHERE emprendedora_id = :emprendedoraId ORDER BY id DESC")
    fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<ProductoEntity>>

    // Busca un producto por su id de forma reactiva
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): Flow<ProductoEntity?>

    // Busca un producto por su id de forma puntual
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorIdDirecto(id: Int): ProductoEntity?

    // Retorna el catálogo completo disponible
    @Query("SELECT * FROM productos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<ProductoEntity>>

    // Guarda un producto nuevo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoEntity): Long

    // Carga productos iniciales de demostración
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(productos: List<ProductoEntity>)

    // Actualiza la información de un producto
    @Update
    suspend fun actualizar(producto: ProductoEntity): Int

    // Elimina un producto por su identificador
    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarPorId(id: Int): Int

    // Marca todos los productos como respaldados, quitando la marca de pendiente de subir
    @Query("UPDATE productos SET pendiente_sincronizar = 0 WHERE pendiente_sincronizar = 1")
    suspend fun marcarTodosSincronizados(): Int

    // Limpia el estado pendiente de un producto específico
    @Query("UPDATE productos SET pendiente_sincronizar = 0 WHERE id = :id")
    suspend fun limpiarPendienteProducto(id: Int): Int
}
