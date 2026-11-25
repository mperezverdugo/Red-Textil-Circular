package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.MensajeDao
import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.MercadoApi
import com.example.textilcircular.data.remote.dto.CrearConversacionRequestDto
import com.example.textilcircular.data.remote.dto.EnviarMensajeRequestDto
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Implementación del repositorio de mensajería interna y conversaciones conectada a SQLite y a ms-mercado vía Retrofit.
class MensajeRepositoryRoom(
    private val mensajeDao: MensajeDao,
    private val productoDao: ProductoDao,
    private val clienteDao: ClienteDao? = null,
    private val emprendedoraDao: EmprendedoraDao? = null,
    private val mercadoApi: MercadoApi? = null
) : MensajeRepository {

    // Conversaciones para la bandeja de la emprendedora
    override fun obtenerConversaciones(emprendedoraId: Int): Flow<List<Conversacion>> = flow {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null && token != null) {
            try {
                val respuesta = mercadoApi.listarConversaciones("Bearer $token")
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val remota = respuesta.body()!!.map { dto ->
                        val prod = dto.productoId?.let { productoDao.obtenerPorIdDirecto(it.toInt())?.aModelo() }
                        Conversacion(
                            id = dto.id.toInt(),
                            emprendedoraId = dto.emprendedoraId.toInt(),
                            clienteId = dto.clienteId.toInt(),
                            nombreCliente = dto.nombreCliente,
                            nombreEmprendimiento = dto.nombreEmprendimiento,
                            ultimoMensaje = dto.ultimoMensaje,
                            fechaHora = dto.fechaHora,
                            noLeidosEmprendedora = dto.noLeidosEmprendedora,
                            noLeidosCliente = dto.noLeidosCliente,
                            productoAsociado = prod
                        )
                    }
                    try {
                        mensajeDao.insertarConversaciones(remota.map { ConversacionEntity.desdeModelo(it) })
                    } catch (_: Exception) {}
                    emit(remota)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }

        mensajeDao.obtenerConversacionesEmprendedora(emprendedoraId).collect { lista ->
            emit(lista.map { entidad -> entidad.aModelo() })
        }
    }

    // Conversaciones para la bandeja del cliente
    override fun obtenerConversacionesCliente(clienteId: Int): Flow<List<Conversacion>> = flow {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null && token != null) {
            try {
                val respuesta = mercadoApi.listarConversaciones("Bearer $token")
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val remota = respuesta.body()!!.map { dto ->
                        val prod = dto.productoId?.let { productoDao.obtenerPorIdDirecto(it.toInt())?.aModelo() }
                        Conversacion(
                            id = dto.id.toInt(),
                            emprendedoraId = dto.emprendedoraId.toInt(),
                            clienteId = dto.clienteId.toInt(),
                            nombreCliente = dto.nombreCliente,
                            nombreEmprendimiento = dto.nombreEmprendimiento,
                            ultimoMensaje = dto.ultimoMensaje,
                            fechaHora = dto.fechaHora,
                            noLeidosEmprendedora = dto.noLeidosEmprendedora,
                            noLeidosCliente = dto.noLeidosCliente,
                            productoAsociado = prod
                        )
                    }
                    try {
                        mensajeDao.insertarConversaciones(remota.map { ConversacionEntity.desdeModelo(it) })
                    } catch (_: Exception) {}
                    emit(remota)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }

        mensajeDao.obtenerConversacionesCliente(clienteId).collect { lista ->
            emit(lista.map { entidad -> entidad.aModelo() })
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
    override fun obtenerMensajes(conversacionId: Int): Flow<List<Mensaje>> = flow {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null && token != null) {
            try {
                val respuesta = mercadoApi.listarMensajes(
                    autorizacion = "Bearer $token",
                    id = conversacionId.toLong()
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val lista = respuesta.body()!!.map { dto ->
                        Mensaje(
                            id = dto.id.toInt(),
                            conversacionId = dto.conversacionId.toInt(),
                            emisor = try { TipoEmisor.valueOf(dto.emisor) } catch (_: Exception) { TipoEmisor.CLIENTE },
                            contenido = dto.contenido,
                            fechaHora = dto.fechaHora,
                            esLeido = dto.esLeido,
                            productoId = dto.productoId?.toInt(),
                            nombreProducto = dto.nombreProducto
                        )
                    }
                    try {
                        if (mensajeDao.obtenerConversacionPorIdDirecto(conversacionId) == null) {
                            mensajeDao.insertarConversacion(
                                ConversacionEntity(
                                    id = conversacionId,
                                    clienteId = 14,
                                    emprendedoraId = 2,
                                    nombreCliente = "Cliente",
                                    nombreEmprendimiento = "Emprendimiento",
                                    ultimoMensaje = lista.lastOrNull()?.contenido ?: "",
                                    fechaHora = lista.lastOrNull()?.fechaHora ?: "",
                                    noLeidosCliente = 0,
                                    noLeidosEmprendedora = 0
                                )
                            )
                        }
                        mensajeDao.insertarMensajes(lista.map { MensajeEntity.desdeModelo(it) })
                    } catch (_: Exception) {}
                    emit(lista)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }

        mensajeDao.obtenerMensajesPorConversacion(conversacionId).collect { lista ->
            emit(lista.map { it.aModelo() })
        }
    }

    // Consulta mensajes nuevos con despuesDeId para el polling incremental y los persiste en SQLite
    override suspend fun sincronizarMensajes(conversacionId: Int, despuesDeId: Long?): Resultado<List<Mensaje>> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null && token != null) {
            try {
                val respuesta = mercadoApi.listarMensajes(
                    autorizacion = "Bearer $token",
                    id = conversacionId.toLong(),
                    despuesDeId = despuesDeId
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val nuevos = respuesta.body()!!.map { dto ->
                        Mensaje(
                            id = dto.id.toInt(),
                            conversacionId = dto.conversacionId.toInt(),
                            emisor = try { TipoEmisor.valueOf(dto.emisor) } catch (_: Exception) { TipoEmisor.CLIENTE },
                            contenido = dto.contenido,
                            fechaHora = dto.fechaHora,
                            esLeido = dto.esLeido,
                            productoId = dto.productoId?.toInt(),
                            nombreProducto = dto.nombreProducto
                        )
                    }
                    if (nuevos.isNotEmpty()) {
                        try {
                            if (mensajeDao.obtenerConversacionPorIdDirecto(conversacionId) == null) {
                                mensajeDao.insertarConversacion(
                                    ConversacionEntity(
                                        id = conversacionId,
                                        clienteId = 14,
                                        emprendedoraId = 2,
                                        nombreCliente = "Cliente",
                                        nombreEmprendimiento = "Emprendimiento",
                                        ultimoMensaje = nuevos.last().contenido,
                                        fechaHora = nuevos.last().fechaHora,
                                        noLeidosCliente = 0,
                                        noLeidosEmprendedora = 0
                                    )
                                )
                            }
                            mensajeDao.insertarMensajes(nuevos.map { MensajeEntity.desdeModelo(it) })
                        } catch (_: Exception) {}
                    }
                    return@withContext Resultado.Exito(nuevos)
                } else if (respuesta.code() in 400..499) {
                    val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                    return@withContext Resultado.Error(errorMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        Resultado.Exito(emptyList())
    }

    // Envía un mensaje, lo sincroniza con ms-mercado y lo respalda en SQLite
    override suspend fun enviarMensaje(
        conversacionId: Int,
        contenido: String,
        emisor: TipoEmisor,
        productoId: Int?,
        nombreProducto: String?
    ): Resultado<Mensaje> = withContext(Dispatchers.IO) {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())
        val token = ClienteRetrofit.tokenActivo

        if (mercadoApi != null) {
            if (token.isNullOrBlank()) {
                return@withContext Resultado.Error("Se requiere una sesión activa con token para enviar mensajes al servidor")
            }
            try {
                val respuesta = mercadoApi.enviarMensaje(
                    autorizacion = "Bearer $token",
                    id = conversacionId.toLong(),
                    solicitud = EnviarMensajeRequestDto(
                        contenido = contenido.trim(),
                        productoId = productoId?.toLong(),
                        nombreProducto = nombreProducto
                    )
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val dto = respuesta.body()!!
                    val mensaje = Mensaje(
                        id = dto.id.toInt(),
                        conversacionId = dto.conversacionId.toInt(),
                        emisor = try { TipoEmisor.valueOf(dto.emisor) } catch (_: Exception) { emisor },
                        contenido = dto.contenido,
                        fechaHora = dto.fechaHora,
                        esLeido = dto.esLeido,
                        productoId = dto.productoId?.toInt(),
                        nombreProducto = dto.nombreProducto
                    )
                    try {
                        val convActual = mensajeDao.obtenerConversacionPorIdDirecto(conversacionId)
                        if (convActual == null) {
                            mensajeDao.insertarConversacion(
                                ConversacionEntity(
                                    id = conversacionId,
                                    clienteId = 14,
                                    emprendedoraId = 2,
                                    nombreCliente = "Cliente",
                                    nombreEmprendimiento = "Emprendimiento",
                                    ultimoMensaje = contenido.trim(),
                                    fechaHora = dto.fechaHora,
                                    noLeidosEmprendedora = if (emisor == TipoEmisor.CLIENTE) 1 else 0,
                                    noLeidosCliente = if (emisor == TipoEmisor.EMPRENDEDORA) 1 else 0
                                )
                            )
                        } else {
                            val nuevosNoLeidosEmp = if (emisor == TipoEmisor.CLIENTE) convActual.noLeidosEmprendedora + 1 else convActual.noLeidosEmprendedora
                            val nuevosNoLeidosCli = if (emisor == TipoEmisor.EMPRENDEDORA) convActual.noLeidosCliente + 1 else convActual.noLeidosCliente

                            mensajeDao.actualizarConversacion(
                                convActual.copy(
                                    ultimoMensaje = contenido.trim(),
                                    fechaHora = dto.fechaHora,
                                    noLeidosEmprendedora = nuevosNoLeidosEmp,
                                    noLeidosCliente = nuevosNoLeidosCli
                                )
                            )
                        }
                        mensajeDao.insertarMensaje(MensajeEntity.desdeModelo(mensaje))
                    } catch (_: Exception) {}
                    return@withContext Resultado.Exito(mensaje)
                } else {
                    val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                    return@withContext Resultado.Error(errorMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                return@withContext Resultado.Error("Error al enviar mensaje al servidor: ${e.message}")
            }
        }

        val mensajeEntity = MensajeEntity(
            id = 0,
            conversacionId = conversacionId,
            emisor = emisor,
            contenido = contenido.trim(),
            fechaHora = horaActual,
            esLeido = false,
            productoId = productoId,
            nombreProducto = nombreProducto
        )

        try {
            val nuevoMsgId = mensajeDao.insertarMensaje(mensajeEntity).toInt()

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

            Resultado.Exito(mensajeEntity.copy(id = nuevoMsgId).aModelo())
        } catch (e: Exception) {
            Resultado.Error("No se pudo enviar el mensaje: ${e.message}")
        }
    }

    // Obtiene la conversación única entre cliente y emprendedora o la crea en ms-mercado y en SQLite
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
        val token = ClienteRetrofit.tokenActivo

        if (mercadoApi != null) {
            if (token.isNullOrBlank()) {
                return@withContext Resultado.Error("Se requiere una sesión activa con token para iniciar la conversación en el servidor")
            }
            try {
                val respuesta = mercadoApi.crearOObtenerConversacion(
                    autorizacion = "Bearer $token",
                    solicitud = CrearConversacionRequestDto(
                        emprendedoraId = emprendedoraId.toLong(),
                        nombreCliente = nombreCliente.trim(),
                        nombreEmprendimiento = nombreEmprendimiento.trim(),
                        productoId = productoAsociado?.id?.toLong()
                    )
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val dto = respuesta.body()!!
                    val conv = Conversacion(
                        id = dto.id.toInt(),
                        emprendedoraId = dto.emprendedoraId.toInt(),
                        clienteId = dto.clienteId.toInt(),
                        nombreCliente = dto.nombreCliente,
                        nombreEmprendimiento = dto.nombreEmprendimiento,
                        ultimoMensaje = dto.ultimoMensaje,
                        fechaHora = dto.fechaHora,
                        noLeidosCliente = dto.noLeidosCliente,
                        noLeidosEmprendedora = dto.noLeidosEmprendedora,
                        productoAsociado = productoAsociado
                    )
                    try {
                        mensajeDao.insertarConversacion(ConversacionEntity.desdeModelo(conv))
                    } catch (_: Exception) {}

                    if (primerMensaje.isNotBlank()) {
                        val respMsg = mercadoApi.enviarMensaje(
                            autorizacion = "Bearer $token",
                            id = conv.id.toLong(),
                            solicitud = EnviarMensajeRequestDto(
                                contenido = primerMensaje.trim(),
                                productoId = productoAsociado?.id?.toLong(),
                                nombreProducto = productoAsociado?.nombre
                            )
                        )
                        if (respMsg.isSuccessful && respMsg.body() != null) {
                            val msgDto = respMsg.body()!!
                            try {
                                mensajeDao.insertarMensaje(
                                    MensajeEntity(
                                        id = msgDto.id.toInt(),
                                        conversacionId = msgDto.conversacionId.toInt(),
                                        emisor = try { TipoEmisor.valueOf(msgDto.emisor) } catch (_: Exception) { emisor },
                                        contenido = msgDto.contenido,
                                        fechaHora = msgDto.fechaHora,
                                        esLeido = msgDto.esLeido,
                                        productoId = msgDto.productoId?.toInt(),
                                        nombreProducto = msgDto.nombreProducto
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                    }
                    return@withContext Resultado.Exito(conv)
                } else {
                    val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                    return@withContext Resultado.Error(errorMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                return@withContext Resultado.Error("Error al conectar con el servidor de mercado: ${e.message}")
            }
        }

        // Aseguramos la existencia de las claves foráneas si están disponibles los DAOs
        if (clienteDao != null && clienteDao.obtenerPorIdDirecto(clienteId) == null) {
            val clienteFallback = DatosDemo.ClientesDemo.find { it.id == clienteId } ?: DatosDemo.ClientesDemo.first()
            clienteDao.insertar(ClienteEntity.desdeModelo(clienteFallback.copy(id = clienteId, nombre = nombreCliente)))
        }
        if (emprendedoraDao != null && emprendedoraDao.obtenerPorIdDirecto(emprendedoraId) == null) {
            val empFallback = DatosDemo.EmprendedorasActivasDemo.find { it.id == emprendedoraId } ?: DatosDemo.EmprendedoraDemo
            emprendedoraDao.insertar(EmprendedoraEntity.desdeModelo(empFallback.copy(id = emprendedoraId, nombreComercial = nombreEmprendimiento)))
        }

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
                        esLeido = false,
                        productoId = productoAsociado?.id,
                        nombreProducto = productoAsociado?.nombre
                    )
                )
            }

            Resultado.Exito(nuevaEntidad.copy(id = nuevoId).aModelo(productoAsociado))
        } catch (e: Exception) {
            Resultado.Error("No se pudo iniciar la conversación: ${e.message}")
        }
    }

    override suspend fun marcarComoLeidoEmprendedora(conversacionId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null && token != null) {
            try {
                mercadoApi.marcarLectura(
                    autorizacion = "Bearer $token",
                    id = conversacionId.toLong()
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        mensajeDao.limpiarNoLeidosEmprendedora(conversacionId)
        Resultado.Exito(Unit)
    }

    override suspend fun marcarComoLeidoCliente(conversacionId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null && token != null) {
            try {
                mercadoApi.marcarLectura(
                    autorizacion = "Bearer $token",
                    id = conversacionId.toLong()
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        mensajeDao.limpiarNoLeidosCliente(conversacionId)
        Resultado.Exito(Unit)
    }

    override suspend fun marcarComoLeido(conversacionId: Int): Resultado<Unit> {
        return marcarComoLeidoEmprendedora(conversacionId)
    }
}
