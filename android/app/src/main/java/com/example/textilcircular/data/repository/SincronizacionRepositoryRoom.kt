package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.dao.SincronizacionDao
import com.example.textilcircular.data.local.entity.SincronizacionEntity
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.ItemSincronizacion
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// Implementación del repositorio de sincronización conectada a la tabla local en SQLite.
class SincronizacionRepositoryRoom(
    private val sincronizacionDao: SincronizacionDao,
    private val productoDao: ProductoDao? = null
) : SincronizacionRepository {

    private val _modoSinConexion = MutableStateFlow(false)
    override val modoSinConexion: StateFlow<Boolean> = _modoSinConexion.asStateFlow()

    override fun setModoSinConexion(activado: Boolean) {
        _modoSinConexion.value = activado
    }

    private val _estadoSincronizacion = MutableStateFlow(EstadoSincronizacion.PENDIENTE)
    override val estadoSincronizacion: StateFlow<EstadoSincronizacion> = _estadoSincronizacion.asStateFlow()

    // Retorna la lista de elementos pendientes de respaldo en segundo plano
    override fun obtenerPendientes(): Flow<List<ItemSincronizacion>> {
        return sincronizacionDao.obtenerPendientes().map { lista ->
            lista.map { it.aModelo() }
        }
    }

    // Registra un nuevo cambio local pendiente de subida
    override suspend fun registrarPendiente(tipo: String, detalle: String) = withContext(Dispatchers.IO) {
        sincronizacionDao.insertar(
            SincronizacionEntity(
                id = 0,
                tipoElemento = tipo,
                detalle = detalle,
                estado = EstadoSincronizacion.PENDIENTE
            )
        )
        _estadoSincronizacion.value = EstadoSincronizacion.PENDIENTE
    }

    // Vacía la cola local y simula el éxito del respaldo
    override suspend fun sincronizar(): Resultado<Int> = withContext(Dispatchers.IO) {
        _estadoSincronizacion.value = EstadoSincronizacion.SINCRONIZANDO

        val total = sincronizacionDao.contarPendientes()
        sincronizacionDao.eliminarTodos()
        productoDao?.marcarTodosSincronizados()

        // TODO: cuando haya servidor, conectar con la API REST y enviar la cola en segundo plano
        _estadoSincronizacion.value = EstadoSincronizacion.SINCRONIZADO
        Resultado.Exito(total)
    }
}
