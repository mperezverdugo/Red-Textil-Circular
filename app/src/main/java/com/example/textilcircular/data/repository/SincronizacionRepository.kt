package com.example.textilcircular.data.repository

import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.ItemSincronizacion
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Contrato para inspeccionar datos pendientes de respaldo y forzar la sincronización.
interface SincronizacionRepository {
    val estadoSincronizacion: StateFlow<EstadoSincronizacion>
    val modoSinConexion: StateFlow<Boolean>
    fun setModoSinConexion(activado: Boolean)
    fun obtenerPendientes(): Flow<List<ItemSincronizacion>>
    suspend fun registrarPendiente(tipo: String, detalle: String)
    suspend fun sincronizar(): Resultado<Int>
}

// Implementación en memoria que simula la cola de elementos pendientes de subida.
class SincronizacionRepositoryMemoria : SincronizacionRepository {

    private val pendientes = mutableListOf<ItemSincronizacion>()
    private val _pendientesFlow = MutableStateFlow<List<ItemSincronizacion>>(pendientes.toList())

    private val _modoSinConexion = MutableStateFlow(false)
    override val modoSinConexion: StateFlow<Boolean> = _modoSinConexion.asStateFlow()

    override fun setModoSinConexion(activado: Boolean) {
        _modoSinConexion.value = activado
    }

    private val _estadoSincronizacion = MutableStateFlow(EstadoSincronizacion.PENDIENTE)
    override val estadoSincronizacion: StateFlow<EstadoSincronizacion> = _estadoSincronizacion.asStateFlow()

    override fun obtenerPendientes(): Flow<List<ItemSincronizacion>> = _pendientesFlow.asStateFlow()

    override suspend fun registrarPendiente(tipo: String, detalle: String) {
        val nuevoId = (pendientes.maxOfOrNull { it.id } ?: 0) + 1
        pendientes.add(
            ItemSincronizacion(
                id = nuevoId,
                tipoElemento = tipo,
                detalle = detalle,
                estado = EstadoSincronizacion.PENDIENTE
            )
        )
        _pendientesFlow.value = pendientes.toList()
        _estadoSincronizacion.value = EstadoSincronizacion.PENDIENTE
    }

    override suspend fun sincronizar(): Resultado<Int> {
        _estadoSincronizacion.value = EstadoSincronizacion.SINCRONIZANDO
        val total = pendientes.size
        // TODO: conectar con SQLite y sincronizar con el servidor en segundo plano
        pendientes.clear()
        _pendientesFlow.value = emptyList()
        _estadoSincronizacion.value = EstadoSincronizacion.SINCRONIZADO
        return Resultado.Exito(total)
    }
}
