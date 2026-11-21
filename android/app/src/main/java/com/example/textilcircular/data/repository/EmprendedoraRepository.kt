package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Contrato para la lectura y modificación de emprendedoras registradas.
interface EmprendedoraRepository {
    fun obtenerTodas(): Flow<List<Emprendedora>>
    fun obtenerPorId(id: Int): Flow<Emprendedora?>
    fun obtenerMercado(
        textoBusqueda: String = "",
        region: String? = null,
        comuna: String? = null,
        rubro: Rubro? = null
    ): Flow<List<Emprendedora>>
    suspend fun guardar(emprendedora: Emprendedora): Resultado<Emprendedora>
    suspend fun actualizar(emprendedora: Emprendedora): Resultado<Unit>
    suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Resultado<Unit>
    suspend fun darDeBaja(id: Int): Resultado<Unit>
}

// Implementación en memoria que simula la persistencia de emprendedoras.
class EmprendedoraRepositoryMemoria : EmprendedoraRepository {

    private val lista = DatosDemo.EmprendimientosAdminDemo.map { it.emprendedora }.toMutableList()
    private val _emprendedoras = MutableStateFlow<List<Emprendedora>>(lista.toList())

    override fun obtenerTodas(): Flow<List<Emprendedora>> = _emprendedoras.asStateFlow()

    override fun obtenerPorId(id: Int): Flow<Emprendedora?> {
        return _emprendedoras.map { list -> list.firstOrNull { it.id == id } }
    }

    override fun obtenerMercado(
        textoBusqueda: String,
        region: String?,
        comuna: String?,
        rubro: Rubro?
    ): Flow<List<Emprendedora>> {
        val textoLimpio = textoBusqueda.trim().lowercase()
        return _emprendedoras.map { list ->
            // Mostramos solo las emprendedoras que no están ocultas ni dadas de baja
            list.filter { it.activa }
                .filter { emp ->
                    if (textoLimpio.isBlank()) true
                    else {
                        emp.nombreComercial.lowercase().contains(textoLimpio) ||
                                emp.comuna.lowercase().contains(textoLimpio) ||
                                emp.region.lowercase().contains(textoLimpio) ||
                                emp.rubros.any { it.nombre.lowercase().contains(textoLimpio) }
                    }
                }
                .filter { emp ->
                    if (region.isNullOrBlank()) true
                    else emp.region.equals(region, ignoreCase = true)
                }
                .filter { emp ->
                    if (comuna.isNullOrBlank()) true
                    else emp.comuna.equals(comuna, ignoreCase = true)
                }
                .filter { emp ->
                    if (rubro == null) true
                    else emp.rubros.contains(rubro)
                }
                .sortedBy { it.nombreComercial.lowercase() }
        }
    }

    override suspend fun guardar(emprendedora: Emprendedora): Resultado<Emprendedora> {
        // TODO: guardar en SQLite y enviar al servidor
        val nuevoId = if (emprendedora.id <= 0) {
            (lista.maxOfOrNull { it.id } ?: 0) + 1
        } else {
            emprendedora.id
        }
        val guardada = emprendedora.copy(id = nuevoId)
        val indice = lista.indexOfFirst { it.id == nuevoId }
        if (indice >= 0) {
            lista[indice] = guardada
        } else {
            lista.add(guardada)
        }
        _emprendedoras.value = lista.toList()
        return Resultado.Exito(guardada)
    }

    override suspend fun actualizar(emprendedora: Emprendedora): Resultado<Unit> {
        // TODO: actualizar en SQLite y sincronizar con el servidor
        val indice = lista.indexOfFirst { it.id == emprendedora.id }
        return if (indice >= 0) {
            lista[indice] = emprendedora
            _emprendedoras.value = lista.toList()
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("No se encontró la emprendedora para actualizar")
        }
    }

    override suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Resultado<Unit> {
        val indice = lista.indexOfFirst { it.id == id }
        return if (indice >= 0) {
            lista[indice] = lista[indice].copy(activa = activa)
            _emprendedoras.value = lista.toList()
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("Emprendedora no encontrada")
        }
    }

    override suspend fun darDeBaja(id: Int): Resultado<Unit> {
        // TODO: registrar la baja voluntaria en SQLite y notificar al servidor
        return cambiarVisibilidad(id, activa = false)
    }
}
