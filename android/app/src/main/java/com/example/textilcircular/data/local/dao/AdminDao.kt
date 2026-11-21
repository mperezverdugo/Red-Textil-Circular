package com.example.textilcircular.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.textilcircular.data.local.entity.CorreoAutomatizacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.model.EstadoCorreo
import kotlinx.coroutines.flow.Flow

// Clase de soporte para recibir conteos agrupados desde consultas SQL
data class ConteoAgrupado(
    val nombre: String,
    val total: Int
)

// Consultas y operaciones para el panel de administración, moderación y métricas.
@Dao
interface AdminDao {

    // Lista todas las fichas de emprendedoras registradas
    @Query("SELECT * FROM emprendedoras ORDER BY LOWER(nombre_comercial) ASC")
    fun obtenerFichas(): Flow<List<EmprendedoraEntity>>

    // Busca una ficha específica por su id
    @Query("SELECT * FROM emprendedoras WHERE id = :id LIMIT 1")
    fun obtenerFichaPorId(id: Int): Flow<EmprendedoraEntity?>

    // Inserta un evento de interacción (búsqueda, clic o consulta)
    @Insert
    suspend fun registrarEvento(evento: MetricaEventoEntity): Long

    // Cuenta cuántos eventos se han registrado de un tipo específico
    @Query("SELECT COUNT(*) FROM metricas_eventos WHERE tipo_evento = :tipo")
    fun contarEventos(tipo: String): Flow<Int>

    // Cuenta cuántas fichas están marcadas como pendientes de revisión
    @Query("SELECT COUNT(*) FROM emprendedoras WHERE estado_ficha = 'POR_REVISAR'")
    fun contarFichasPorRevisar(): Flow<Int>

    // Conteo agrupado de contactos según la región de la artesana
    @Query("""
        SELECT region AS nombre, COUNT(*) AS total 
        FROM metricas_eventos 
        WHERE tipo_evento IN ('CLIC_CONTACTO', 'CONSULTA_ENVIADA') AND region != '' 
        GROUP BY region 
        ORDER BY total DESC
    """)
    fun contarContactosPorRegion(): Flow<List<ConteoAgrupado>>

    // Conteo agrupado de contactos según el rubro textil
    @Query("""
        SELECT rubro AS nombre, COUNT(*) AS total 
        FROM metricas_eventos 
        WHERE tipo_evento IN ('CLIC_CONTACTO', 'CONSULTA_ENVIADA') AND rubro != '' 
        GROUP BY rubro 
        ORDER BY total DESC
    """)
    fun contarContactosPorRubro(): Flow<List<ConteoAgrupado>>

    // Registros de automatización de correos
    @Query("SELECT * FROM correos_automatizaciones ORDER BY id DESC")
    fun obtenerCorreos(): Flow<List<CorreoAutomatizacionEntity>>

    // Guarda un registro de correo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCorreo(correo: CorreoAutomatizacionEntity): Long

    // Carga inicial de registros de correo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCorreos(correos: List<CorreoAutomatizacionEntity>)

    // Actualiza el estado cuando un correo es reenviado exitosamente
    @Query("UPDATE correos_automatizaciones SET estado = :estado, fecha_envio = :fechaEnvio WHERE id = :id")
    suspend fun actualizarEstadoCorreo(id: Int, estado: EstadoCorreo, fechaEnvio: String): Int
}
