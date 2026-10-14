package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.model.Producto
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// Implementación del repositorio de productos respaldada en la tabla productos de SQLite.
class ProductoRepositoryRoom(
    private val productoDao: ProductoDao
) : ProductoRepository {

    // Retorna todos los productos creados por la artesana especificada
    override fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<Producto>> {
        return productoDao.obtenerPorEmprendedora(emprendedoraId).map { lista ->
            lista.map { it.aModelo() }
        }
    }

    // Busca un producto por su identificador único
    override fun obtenerPorId(id: Int): Flow<Producto?> {
        return productoDao.obtenerPorId(id).map { it?.aModelo() }
    }

    // Guarda una nueva creación textil asegurando su relación con la emprendedora y rutas de fotos
    override suspend fun guardar(producto: Producto): Resultado<Producto> = withContext(Dispatchers.IO) {
        try {
            val entidad = ProductoEntity.desdeModelo(producto)
            val nuevoId = productoDao.insertar(entidad).toInt()
            val idFinal = if (producto.id > 0) producto.id else nuevoId

            // TODO: cuando haya servidor, sincronizar producto creado con la API REST
            Resultado.Exito(producto.copy(id = idFinal))
        } catch (e: Exception) {
            Resultado.Error("No se pudo guardar el producto en la base de datos: ${e.message}")
        }
    }

    // Actualiza los datos de un producto en SQLite
    override suspend fun actualizar(producto: Producto): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val filas = productoDao.actualizar(ProductoEntity.desdeModelo(producto))
            if (filas > 0) {
                // TODO: cuando haya servidor, notificar actualización de producto a la API REST
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("Producto no encontrado para actualizar")
            }
        } catch (e: Exception) {
            Resultado.Error("Error al actualizar el producto: ${e.message}")
        }
    }

    // Elimina el producto por id de la base de datos local
    override suspend fun eliminar(id: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val eliminadas = productoDao.eliminarPorId(id)
            if (eliminadas > 0) {
                // TODO: cuando haya servidor, registrar baja del producto en la API REST
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("No se pudo encontrar el producto a eliminar")
            }
        } catch (e: Exception) {
            Resultado.Error("Error al eliminar el producto: ${e.message}")
        }
    }
}
