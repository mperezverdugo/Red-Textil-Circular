package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.MensajeDao
import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Implementación del repositorio de mensajería interna y conversaciones sobre SQLite.
class MensajeRepositoryRoom(
    private val mensajeDao: MensajeDao,
    private val productoDao: ProductoDao
) : MensajeRepository {

    // Conversaciones para la bandeja de la emprendedora
    override fun obtenerConversaciones(emprendedoraId: Int): Flow<List<Conversacion>> {
        return mensajeDao.obtenerConversacionesEmprendedora(emprendedoraId).map { lista ->
            lista.map { entidad -> entidad.aModelo() }
        }
    }

    // Conversaciones para la bandeja del cliente
    override fun obtenerConversacionesCliente(clienteId: Int): Flow<List<Conversacion>> {
        return mensajeDao.obtenerConversacionesCliente(clienteId).map { lista ->
            lista.map { entidad -> entidad.aModelo() }
        }
    }

    // Retorna una conversación por id junto al producto referenciado si existe
    override fun obtenerConversacionPorId(conversacionId: Int): Flow<Conversacion?> {
        return mensajeDao.obtenerConversacionPorId(conversacionId).map { entidad ->
            if (entidad == null) null
            else {
                val prod = entidad.productoId?.let { productoDao.obtenerPorIdDirecto(it)?.aModelo() }
                entidad.aModelo(prod)
            }
        }
    }

    // Busca la conversación activa entre una pareja específica
    override fun obtenerConversacionEntre(clienteId: Int, emprendedoraId: Int): Flow<Conversacion?> {
        return mensajeDao.obtenerConversacionEntre(clienteId, emprendedoraId).map { entidad ->
            if (entidad == null) null
            else {
                val prod = entidad.productoId?.let { productoDao.obtenerPorIdDirecto(it)?.aModelo() }
                entidad.aModelo(prod)
            }
        }
    }

    // Mensajes ordenados por fecha y hora dentro del chat
    override fun obtenerMensajes(conversacionId: Int): Flow<List<Mensaje>> {
        return mensajeDao.obtenerMensajesPorConversacion(conversacionId).map { lista ->
            lista.map { it.aModelo() }
        }
    }

    // Envía un mensaje, lo persiste en la tabla mensajes y actualiza el encabezado de la conversación
    override suspend fun enviarMensaje(
        conversacionId: Int,
        contenido: String,
        emisor: TipoEmisor,
        productoId: Int?,
        nombreProducto: String?
    ): Resultado<Mensaje> = withContext(Dispatchers.IO) {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        val mensajeEntity = MensajeEntity(
            id = 0,
            conversacionId = conversacionId,
            emisor = emisor,
            contenido = contenido.trim(),
            fechaHora = horaActual,
            esLeido = true,
            productoId = productoId,
            nombreProducto = nombreProducto
        )

        try {
            val nuevoMsgId = mensajeDao.insertarMensaje(mensajeEntity).toInt()

            // Actualizamos el último mensaje y el contador de no leídos de la otra parte
            val convActual = mensajeDao.obtenerConversacionPorIdDirecto(conversacionId)
            if (convActual != null) {
                val nuevosNoLeidosEmp = if (emisor == TipoEmisor.CLIENTE) convActual.noLeidosEmprendedora + 1 else convActual.noLeidosEmprendedora
                val nuevosNoLeidosCli = if (emisor == TipoEmisor.EMPRENDEDORA) convActual.noLeidosCliente + 1 else convActual.noLeidosCliente

                mensajeDao.actualizarConversacion(
                    convActual.copy(
                        ultimoMensaje = contenido.trim(),
                        fechaHora = horaActual,
                        noLeidosEmprendedora = nuevosNoLeidosEmp,
                        noLeidosCliente = nuevosNoLeidosCli
                    )
                )
            }

            // TODO: cuando haya servidor, sincronizar mensaje vía API REST o WebSocket
            Resultado.Exito(mensajeEntity.copy(id = nuevoMsgId).aModelo())
        } catch (e: Exception) {
            Resultado.Error("No se pudo enviar el mensaje: ${e.message}")
        }
    }

    // Obtiene la conversación única entre cliente y emprendedora o la crea en SQLite si es la primera vez
    override suspend fun iniciarOObtenerConversacion(
        clienteId: Int,
        emprendedoraId: Int,
        nombreCliente: String,
        nombreEmprendimiento: String,
        primerMensaje: String,
        emisor: TipoEmisor,
        productoAsociado: Producto?
    ): Resultado<Conversacion> = withContext(Dispatchers.IO) {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        val existente = mensajeDao.obtenerConversacionEntreDirecto(clienteId, emprendedoraId)
        if (existente != null) {
            if (primerMensaje.isNotBlank()) {
                enviarMensaje(
                    conversacionId = existente.id,
                    contenido = primerMensaje,
                    emisor = emisor,
                    productoId = productoAsociado?.id,
                    nombreProducto = productoAsociado?.nombre
                )
            }
            if (productoAsociado != null && existente.productoId == null) {
                mensajeDao.actualizarConversacion(existente.copy(productoId = productoAsociado.id))
            }
            return@withContext Resultado.Exito(existente.aModelo(productoAsociado))
        }

        // Si no existía, creamos la fila única en SQLite
        val nuevaEntidad = ConversacionEntity(
            id = 0,
            clienteId = clienteId,
            emprendedoraId = emprendedoraId,
            productoId = productoAsociado?.id,
            nombreCliente = nombreCliente,
            nombreEmprendimiento = nombreEmprendimiento,
            ultimoMensaje = primerMensaje.ifBlank { "Conversación iniciada" },
            fechaHora = horaActual,
            noLeidosEmprendedora = if (emisor == TipoEmisor.CLIENTE && primerMensaje.isNotBlank()) 1 else 0,
            noLeidosCliente = if (emisor == TipoEmisor.EMPRENDEDORA && primerMensaje.isNotBlank()) 1 else 0
        )

        try {
            val nuevoId = mensajeDao.insertarConversacion(nuevaEntidad).toInt()

            if (primerMensaje.isNotBlank()) {
                mensajeDao.insertarMensaje(
                    MensajeEntity(
                        id = 0,
                        conversacionId = nuevoId,
                        emisor = emisor,
                        contenido = primerMensaje.trim(),
                        fechaHora = horaActual,
                        esLeido = true,
                        productoId = productoAsociado?.id,
                        nombreProducto = productoAsociado?.nombre
                    )
                )
            }

            // TODO: cuando haya servidor, sincronizar creación de conversación con la API REST
            Resultado.Exito(nuevaEntidad.copy(id = nuevoId).aModelo(productoAsociado))
        } catch (e: Exception) {
            Resultado.Error("No se pudo iniciar la conversación: ${e.message}")
        }
    }

    override suspend fun marcarComoLeidoEmprendedora(conversacionId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        mensajeDao.limpiarNoLeidosEmprendedora(conversacionId)
        Resultado.Exito(Unit)
    }

    override suspend fun marcarComoLeidoCliente(conversacionId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        mensajeDao.limpiarNoLeidosCliente(conversacionId)
        Resultado.Exito(Unit)
    }

    override suspend fun marcarComoLeido(conversacionId: Int): Resultado<Unit> {
        return marcarComoLeidoEmprendedora(conversacionId)
    }
}
