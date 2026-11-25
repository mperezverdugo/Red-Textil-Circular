package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.MercadoApi
import com.example.textilcircular.data.remote.dto.CrearProductoRequestDto
import com.example.textilcircular.data.remote.dto.EditarProductoRequestDto
import com.example.textilcircular.model.Producto
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

// Implementación del repositorio de productos conectada a SQLite y a ms-mercado vía Retrofit.
class ProductoRepositoryRoom(
    private val productoDao: ProductoDao,
    private val mercadoApi: MercadoApi? = null
) : ProductoRepository {

    // Retorna todos los productos creados por la artesana especificada (id de usuario)
    override fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<Producto>> = flow {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null) {
            val idRemoto = if (emprendedoraId == 1) 2L else emprendedoraId.toLong()
            try {
                val respuesta = mercadoApi.listarPorEmprendedora(
                    autorizacion = token?.let { "Bearer $it" },
                    emprendedoraId = idRemoto
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val remotos = respuesta.body()!!.map { dto ->
                        Producto(
                            id = dto.id.toInt(),
                            emprendedoraId = dto.emprendedoraId.toInt(),
                            nombre = dto.nombre,
                            descripcionCorta = dto.descripcionCorta,
                            explicacionProceso = dto.explicacionProceso
                        )
                    }
                    try {
                        productoDao.insertarVarios(remotos.map { ProductoEntity.desdeModelo(it) })
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }

        productoDao.obtenerPorEmprendedora(emprendedoraId).collect { lista ->
            emit(lista.map { it.aModelo() })
        }
    }

    // Busca un producto por su identificador único
    override fun obtenerPorId(id: Int): Flow<Producto?> = flow {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null) {
            try {
                val respuesta = mercadoApi.obtenerProducto(
                    autorizacion = token?.let { "Bearer $it" },
                    id = id.toLong()
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val dto = respuesta.body()!!
                    val prod = Producto(
                        id = dto.id.toInt(),
                        emprendedoraId = dto.emprendedoraId.toInt(),
                        nombre = dto.nombre,
                        descripcionCorta = dto.descripcionCorta,
                        explicacionProceso = dto.explicacionProceso
                    )
                    try {
                        productoDao.insertar(ProductoEntity.desdeModelo(prod))
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }

        productoDao.obtenerPorId(id).collect {
            emit(it?.aModelo())
        }
    }

    // Guarda una nueva creación textil sincronizándola con ms-mercado y respaldando en SQLite
    override suspend fun guardar(producto: Producto): Resultado<Producto> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null) {
            if (token.isNullOrBlank()) {
                return@withContext Resultado.Error("Se requiere una sesión activa con token para publicar en el servidor")
            }
            try {
                val respuesta = mercadoApi.crearProducto(
                    autorizacion = "Bearer $token",
                    solicitud = CrearProductoRequestDto(
                        nombre = producto.nombre.trim(),
                        descripcionCorta = producto.descripcionCorta.trim(),
                        explicacionProceso = producto.explicacionProceso?.trim()
                    )
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val dto = respuesta.body()!!
                    val nuevoProducto = producto.copy(
                        id = dto.id.toInt(),
                        emprendedoraId = dto.emprendedoraId.toInt()
                    )
                    try {
                        productoDao.insertar(ProductoEntity.desdeModelo(nuevoProducto))
                    } catch (_: Exception) {}
                    return@withContext Resultado.Exito(nuevoProducto)
                } else {
                    val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                    return@withContext Resultado.Error(errorMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                return@withContext Resultado.Error("Error al conectar con el servidor de mercado: ${e.message}")
            }
        }

        try {
            val entidad = ProductoEntity.desdeModelo(producto)
            val nuevoId = productoDao.insertar(entidad).toInt()
            val idFinal = if (producto.id > 0) producto.id else nuevoId
            Resultado.Exito(producto.copy(id = idFinal))
        } catch (e: Exception) {
            Resultado.Error("No se pudo guardar el producto en la base de datos: ${e.message}")
        }
    }

    // Actualiza los datos de un producto en ms-mercado y en SQLite
    override suspend fun actualizar(producto: Producto): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null) {
            if (token.isNullOrBlank()) {
                return@withContext Resultado.Error("Se requiere una sesión activa con token para actualizar en el servidor")
            }
            try {
                val respuesta = mercadoApi.editarProducto(
                    autorizacion = "Bearer $token",
                    id = producto.id.toLong(),
                    solicitud = EditarProductoRequestDto(
                        nombre = producto.nombre.trim(),
                        descripcionCorta = producto.descripcionCorta.trim(),
                        explicacionProceso = producto.explicacionProceso?.trim()
                    )
                )
                if (respuesta.isSuccessful) {
                    try {
                        productoDao.actualizar(ProductoEntity.desdeModelo(producto))
                    } catch (_: Exception) {}
                    return@withContext Resultado.Exito(Unit)
                } else {
                    val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                    return@withContext Resultado.Error(errorMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                return@withContext Resultado.Error("Error al conectar con el servidor: ${e.message}")
            }
        }

        try {
            val filas = productoDao.actualizar(ProductoEntity.desdeModelo(producto))
            if (filas > 0) {
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("Producto no encontrado para actualizar")
            }
        } catch (e: Exception) {
            Resultado.Error("Error al actualizar el producto: ${e.message}")
        }
    }

    // Elimina el producto por id en ms-mercado y en SQLite
    override suspend fun eliminar(id: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (mercadoApi != null) {
            if (token.isNullOrBlank()) {
                return@withContext Resultado.Error("Se requiere una sesión activa con token para eliminar en el servidor")
            }
            try {
                val respuesta = mercadoApi.eliminarProducto(
                    autorizacion = "Bearer $token",
                    id = id.toLong()
                )
                if (respuesta.isSuccessful) {
                    try {
                        productoDao.eliminarPorId(id)
                    } catch (_: Exception) {}
                    return@withContext Resultado.Exito(Unit)
                } else {
                    val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                    return@withContext Resultado.Error(errorMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                return@withContext Resultado.Error("Error al conectar con el servidor: ${e.message}")
            }
        }

        try {
            val eliminadas = productoDao.eliminarPorId(id)
            if (eliminadas > 0) {
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("No se pudo encontrar el producto a eliminar")
            }
        } catch (e: Exception) {
            Resultado.Error("Error al eliminar el producto: ${e.message}")
        }
    }

    // Marca todos los productos locales como respaldados
    override suspend fun marcarTodosSincronizados() = withContext(Dispatchers.IO) {
        productoDao.marcarTodosSincronizados()
        Unit
    }
}
