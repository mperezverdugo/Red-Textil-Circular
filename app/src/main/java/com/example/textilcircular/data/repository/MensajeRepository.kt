package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Contrato para la mensajería interna entre clientes y emprendedoras.
interface MensajeRepository {
    fun obtenerConversaciones(emprendedoraId: Int): Flow<List<Conversacion>>
    fun obtenerConversacionPorId(conversacionId: Int): Flow<Conversacion?>
    fun obtenerMensajes(conversacionId: Int): Flow<List<Mensaje>>
    suspend fun enviarMensaje(
        conversacionId: Int,
        contenido: String,
        emisor: TipoEmisor = TipoEmisor.EMPRENDEDORA
    ): Resultado<Mensaje>
    suspend fun marcarComoLeido(conversacionId: Int): Resultado<Unit>
}

// Implementación en memoria que simula la mensajería local.
class MensajeRepositoryMemoria : MensajeRepository {

    private val conversaciones = DatosDemo.ConversacionesDemo.toMutableList()
    private val mensajes = DatosDemo.MensajesConversacionDemo.toMutableList()

    private val _conversacionesFlow = MutableStateFlow<List<Conversacion>>(conversaciones.toList())
    private val _mensajesFlow = MutableStateFlow<List<Mensaje>>(mensajes.toList())

    override fun obtenerConversaciones(emprendedoraId: Int): Flow<List<Conversacion>> {
        return _conversacionesFlow.map { list -> list.filter { it.emprendedoraId == emprendedoraId } }
    }

    override fun obtenerConversacionPorId(conversacionId: Int): Flow<Conversacion?> {
        return _conversacionesFlow.map { list -> list.firstOrNull { it.id == conversacionId } }
    }

    override fun obtenerMensajes(conversacionId: Int): Flow<List<Mensaje>> {
        return _mensajesFlow.map { list -> list.filter { it.conversacionId == conversacionId } }
    }

    override suspend fun enviarMensaje(
        conversacionId: Int,
        contenido: String,
        emisor: TipoEmisor
    ): Resultado<Mensaje> {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        val nuevoId = (mensajes.maxOfOrNull { it.id } ?: 0) + 1
        val nuevoMensaje = Mensaje(
            id = nuevoId,
            conversacionId = conversacionId,
            emisor = emisor,
            contenido = contenido.trim(),
            fechaHora = horaActual,
            esLeido = true
        )

        mensajes.add(nuevoMensaje)
        _mensajesFlow.value = mensajes.toList()

        // Actualizar último mensaje de la conversación
        val idxConv = conversaciones.indexOfFirst { it.id == conversacionId }
        if (idxConv >= 0) {
            val convActual = conversaciones[idxConv]
            conversaciones[idxConv] = convActual.copy(
                ultimoMensaje = contenido.trim(),
                fechaHora = horaActual
            )
            _conversacionesFlow.value = conversaciones.toList()
        }

        // TODO: guardar en SQLite y enviar vía WebSocket o API REST al servidor
        return Resultado.Exito(nuevoMensaje)
    }

    override suspend fun marcarComoLeido(conversacionId: Int): Resultado<Unit> {
        val idxConv = conversaciones.indexOfFirst { it.id == conversacionId }
        if (idxConv >= 0) {
            conversaciones[idxConv] = conversaciones[idxConv].copy(mensajesNoLeidos = 0)
            _conversacionesFlow.value = conversaciones.toList()
        }
        return Resultado.Exito(Unit)
    }
}
