package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import kotlinx.coroutines.flow.Flow

// Consultas y operaciones para las conversaciones y mensajes del chat en SQLite.
@Dao
interface MensajeDao {

    // Conversaciones de la bandeja de entrada de una emprendedora
    @Query("SELECT * FROM conversaciones WHERE emprendedora_id = :emprendedoraId ORDER BY id DESC")
    fun obtenerConversacionesEmprendedora(emprendedoraId: Int): Flow<List<ConversacionEntity>>

    // Conversaciones de la bandeja de entrada de un cliente
    @Query("SELECT * FROM conversaciones WHERE cliente_id = :clienteId ORDER BY id DESC")
    fun obtenerConversacionesCliente(clienteId: Int): Flow<List<ConversacionEntity>>

    // Obtiene una conversación por su id de forma reactiva
    @Query("SELECT * FROM conversaciones WHERE id = :id LIMIT 1")
    fun obtenerConversacionPorId(id: Int): Flow<ConversacionEntity?>

    // Obtiene una conversación por su id de forma directa
    @Query("SELECT * FROM conversaciones WHERE id = :id LIMIT 1")
    suspend fun obtenerConversacionPorIdDirecto(id: Int): ConversacionEntity?

    // Busca si ya existe una conversación entre una pareja cliente-emprendedora
    @Query("""
        SELECT * FROM conversaciones 
        WHERE cliente_id = :clienteId AND emprendedora_id = :emprendedoraId 
        LIMIT 1
    """)
    fun obtenerConversacionEntre(clienteId: Int, emprendedoraId: Int): Flow<ConversacionEntity?>

    @Query("""
        SELECT * FROM conversaciones 
        WHERE cliente_id = :clienteId AND emprendedora_id = :emprendedoraId 
        LIMIT 1
    """)
    suspend fun obtenerConversacionEntreDirecto(clienteId: Int, emprendedoraId: Int): ConversacionEntity?

    // Guarda una nueva conversación
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConversacion(conversacion: ConversacionEntity): Long

    // Carga de conversaciones en lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConversaciones(conversaciones: List<ConversacionEntity>)

    // Actualiza el último mensaje y contadores de una conversación
    @Update
    suspend fun actualizarConversacion(conversacion: ConversacionEntity): Int

    // Mensajes ordenados cronológicamente de una conversación
    @Query("SELECT * FROM mensajes WHERE conversacion_id = :conversacionId ORDER BY id ASC")
    fun obtenerMensajesPorConversacion(conversacionId: Int): Flow<List<MensajeEntity>>

    // Guarda un mensaje nuevo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMensaje(mensaje: MensajeEntity): Long

    // Carga de mensajes en lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMensajes(mensajes: List<MensajeEntity>)

    // Marca como leídos los mensajes pendientes de la emprendedora
    @Query("UPDATE conversaciones SET no_leidos_emprendedora = 0 WHERE id = :conversacionId")
    suspend fun limpiarNoLeidosEmprendedora(conversacionId: Int): Int

    // Marca como leídos los mensajes pendientes del cliente
    @Query("UPDATE conversaciones SET no_leidos_cliente = 0 WHERE id = :conversacionId")
    suspend fun limpiarNoLeidosCliente(conversacionId: Int): Int
}
