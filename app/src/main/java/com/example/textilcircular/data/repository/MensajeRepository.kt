package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.Producto
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
    fun obtenerConversacionesCliente(clienteId: Int): Flow<List<Conversacion>>
    fun obtenerConversacionPorId(conversacionId: Int): Flow<Conversacion?>
    fun obtenerConversacionEntre(clienteId: Int, emprendedoraId: Int): Flow<Conversacion?>
    fun obtenerMensajes(conversacionId: Int): Flow<List<Mensaje>>
    suspend fun enviarMensaje(
        conversacionId: Int,
        contenido: String,
        emisor: TipoEmisor = TipoEmisor.EMPRENDEDORA,
        productoId: Int? = null,
        nombreProducto: String? = null
    ): Resultado<Mensaje>
    suspend fun iniciarOObtenerConversacion(
        clienteId: Int,
        emprendedoraId: Int,
        nombreCliente: String,
        nombreEmprendimiento: String,
        primerMensaje: String,
        emisor: TipoEmisor,
        productoAsociado: Producto? = null
    ): Resultado<Conversacion>
    suspend fun marcarComoLeidoEmprendedora(conversacionId: Int): Resultado<Unit>
    suspend fun marcarComoLeidoCliente(conversacionId: Int): Resultado<Unit>
    suspend fun marcarComoLeido(conversacionId: Int): Resultado<Unit>
}

// Implementación en memoria que simula la mensajería interna y conteo de no leídos por lado.
class MensajeRepositoryMemoria : MensajeRepository {

    private val conversaciones = DatosDemo.ConversacionesDemo.toMutableList()
    private val mensajes = DatosDemo.MensajesConversacionDemo.toMutableList()

    private val _conversacionesFlow = MutableStateFlow<List<Conversacion>>(conversaciones.toList())
    private val _mensajesFlow = MutableStateFlow<List<Mensaje>>(mensajes.toList())

    override fun obtenerConversaciones(emprendedoraId: Int): Flow<List<Conversacion>> {
        return _conversacionesFlow.map { list -> list.filter { it.emprendedoraId == emprendedoraId } }
    }

    override fun obtenerConversacionesCliente(clienteId: Int): Flow<List<Conversacion>> {
        return _conversacionesFlow.map { list -> list.filter { it.clienteId == clienteId } }
    }

    override fun obtenerConversacionPorId(conversacionId: Int): Flow<Conversacion?> {
        return _conversacionesFlow.map { list -> list.firstOrNull { it.id == conversacionId } }
    }

    override fun obtenerConversacionEntre(clienteId: Int, emprendedoraId: Int): Flow<Conversacion?> {
        return _conversacionesFlow.map { list ->
            list.firstOrNull { it.clienteId == clienteId && it.emprendedoraId == emprendedoraId }
        }
    }

    override fun obtenerMensajes(conversacionId: Int): Flow<List<Mensaje>> {
        return _mensajesFlow.map { list -> list.filter { it.conversacionId == conversacionId } }
    }

    override suspend fun enviarMensaje(
        conversacionId: Int,
        contenido: String,
        emisor: TipoEmisor,
        productoId: Int?,
        nombreProducto: String?
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
            esLeido = true,
            productoId = productoId,
            nombreProducto = nombreProducto
        )

        mensajes.add(nuevoMensaje)
        _mensajesFlow.value = mensajes.toList()

        // Actualizar último mensaje y contadores de no leídos por lado
        val idxConv = conversaciones.indexOfFirst { it.id == conversacionId }
        if (idxConv >= 0) {
            val convActual = conversaciones[idxConv]
            val nuevosNoLeidosEmp = if (emisor == TipoEmisor.CLIENTE) convActual.noLeidosEmprendedora + 1 else convActual.noLeidosEmprendedora
            val nuevosNoLeidosCli = if (emisor == TipoEmisor.EMPRENDEDORA) convActual.noLeidosCliente + 1 else convActual.noLeidosCliente

            conversaciones[idxConv] = convActual.copy(
                ultimoMensaje = contenido.trim(),
                fechaHora = horaActual,
                noLeidosEmprendedora = nuevosNoLeidosEmp,
                noLeidosCliente = nuevosNoLeidosCli
            )
            _conversacionesFlow.value = conversaciones.toList()
        }

        // TODO: guardar esto en SQLite más adelante
        // TODO: cuando haya servidor, sincronizar mensaje vía API REST o WebSocket
        return Resultado.Exito(nuevoMensaje)
    }

    override suspend fun iniciarOObtenerConversacion(
        clienteId: Int,
        emprendedoraId: Int,
        nombreCliente: String,
        nombreEmprendimiento: String,
        primerMensaje: String,
        emisor: TipoEmisor,
        productoAsociado: Producto?
    ): Resultado<Conversacion> {
        val idxExistente = conversaciones.indexOfFirst { it.clienteId == clienteId && it.emprendedoraId == emprendedoraId }
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        if (idxExistente >= 0) {
            val convExistente = conversaciones[idxExistente]
            if (primerMensaje.isNotBlank()) {
                enviarMensaje(
                    conversacionId = convExistente.id,
                    contenido = primerMensaje,
                    emisor = emisor,
                    productoId = productoAsociado?.id,
                    nombreProducto = productoAsociado?.nombre
                )
            }
            val actualizada = if (productoAsociado != null && convExistente.productoAsociado == null) {
                convExistente.copy(productoAsociado = productoAsociado)
            } else {
                convExistente
            }
            conversaciones[idxExistente] = actualizada
            _conversacionesFlow.value = conversaciones.toList()
            return Resultado.Exito(actualizada)
        }

        // Crear nueva conversación única para la pareja cliente-emprendedora
        val nuevoConvId = (conversaciones.maxOfOrNull { it.id } ?: 200) + 1
        val nuevaConv = Conversacion(
            id = nuevoConvId,
            emprendedoraId = emprendedoraId,
            clienteId = clienteId,
            nombreCliente = nombreCliente,
            nombreEmprendimiento = nombreEmprendimiento,
            ultimoMensaje = primerMensaje.ifBlank { "Conversación iniciada" },
            fechaHora = horaActual,
            noLeidosEmprendedora = if (emisor == TipoEmisor.CLIENTE && primerMensaje.isNotBlank()) 1 else 0,
            noLeidosCliente = if (emisor == TipoEmisor.EMPRENDEDORA && primerMensaje.isNotBlank()) 1 else 0,
            productoAsociado = productoAsociado
        )

        conversaciones.add(nuevaConv)
        _conversacionesFlow.value = conversaciones.toList()

        if (primerMensaje.isNotBlank()) {
            val nuevoMsgId = (mensajes.maxOfOrNull { it.id } ?: 0) + 1
            mensajes.add(
                Mensaje(
                    id = nuevoMsgId,
                    conversacionId = nuevoConvId,
                    emisor = emisor,
                    contenido = primerMensaje.trim(),
                    fechaHora = horaActual,
                    esLeido = true,
                    productoId = productoAsociado?.id,
                    nombreProducto = productoAsociado?.nombre
                )
            )
            _mensajesFlow.value = mensajes.toList()
        }

        // TODO: guardar esto en SQLite más adelante
        return Resultado.Exito(nuevaConv)
    }

    override suspend fun marcarComoLeidoEmprendedora(conversacionId: Int): Resultado<Unit> {
        val idxConv = conversaciones.indexOfFirst { it.id == conversacionId }
        if (idxConv >= 0) {
            conversaciones[idxConv] = conversaciones[idxConv].copy(noLeidosEmprendedora = 0)
            _conversacionesFlow.value = conversaciones.toList()
        }
        return Resultado.Exito(Unit)
    }

    override suspend fun marcarComoLeidoCliente(conversacionId: Int): Resultado<Unit> {
        val idxConv = conversaciones.indexOfFirst { it.id == conversacionId }
        if (idxConv >= 0) {
            conversaciones[idxConv] = conversaciones[idxConv].copy(noLeidosCliente = 0)
            _conversacionesFlow.value = conversaciones.toList()
        }
        return Resultado.Exito(Unit)
    }

    override suspend fun marcarComoLeido(conversacionId: Int): Resultado<Unit> {
        return marcarComoLeidoEmprendedora(conversacionId)
    }
}
