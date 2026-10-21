package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Contrato para gestionar los productos del catálogo de la emprendedora.
interface ProductoRepository {
    fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<Producto>>
    fun obtenerPorId(id: Int): Flow<Producto?>
    suspend fun guardar(producto: Producto): Resultado<Producto>
    suspend fun actualizar(producto: Producto): Resultado<Unit>
    suspend fun eliminar(id: Int): Resultado<Unit>
    suspend fun marcarTodosSincronizados()
}

// Implementación en memoria del repositorio de productos.
class ProductoRepositoryMemoria : ProductoRepository {

    private val lista = DatosDemo.ProductosDemo.toMutableList()
    private val _productos = MutableStateFlow<List<Producto>>(lista.toList())

    override fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<Producto>> {
        return _productos.map { list -> list.filter { it.emprendedoraId == emprendedoraId } }
    }

    override fun obtenerPorId(id: Int): Flow<Producto?> {
        return _productos.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun guardar(producto: Producto): Resultado<Producto> {
        // TODO: guardar en SQLite y marcar como pendiente de sincronizar si no hay red
        val nuevoId = if (producto.id <= 0) {
            (lista.maxOfOrNull { it.id } ?: 100) + 1
        } else {
            producto.id
        }
        val guardado = producto.copy(id = nuevoId)
        val indice = lista.indexOfFirst { it.id == nuevoId }
        if (indice >= 0) {
            lista[indice] = guardado
        } else {
            lista.add(guardado)
        }
        _productos.value = lista.toList()
        return Resultado.Exito(guardado)
    }

    override suspend fun actualizar(producto: Producto): Resultado<Unit> {
        // TODO: actualizar en SQLite y notificar al servidor
        val indice = lista.indexOfFirst { it.id == producto.id }
        return if (indice >= 0) {
            lista[indice] = producto
            _productos.value = lista.toList()
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("Producto no encontrado para actualizar")
        }
    }

    override suspend fun eliminar(id: Int): Resultado<Unit> {
        // TODO: eliminar en SQLite y registrar baja en el servidor
        val eliminado = lista.removeAll { it.id == id }
        return if (eliminado) {
            _productos.value = lista.toList()
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("No se pudo encontrar el producto a eliminar")
        }
    }

    override suspend fun marcarTodosSincronizados() {
        val sincronizados = lista.map { it.copy(pendienteSincronizar = false) }
        lista.clear()
        lista.addAll(sincronizados)
        _productos.value = lista.toList()
    }
}
