package com.example.textilcircular.ui.screens.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.AdminRepository
import com.example.textilcircular.data.repository.EmprendedoraRepository
import com.example.textilcircular.data.repository.ProductoRepository
import com.example.textilcircular.model.ItemMercadoEmprendedora
import com.example.textilcircular.model.Rubro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de exploración del mercado
data class ExplorarUiState(
    val queryTexto: String = "",
    val regionFiltro: String = "",
    val comunaFiltro: String = "",
    val rubroFiltro: String = "",
    val emprendedoras: List<ItemMercadoEmprendedora> = emptyList(),
    val cargando: Boolean = true
)

// Gestiona la búsqueda y filtrado de talleres textiles en el mercado
class ExplorarViewModel(
    private val emprendedoraRepository: EmprendedoraRepository,
    private val productoRepository: ProductoRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExplorarUiState())
    val uiState: StateFlow<ExplorarUiState> = _uiState.asStateFlow()

    init {
        cargarMercado()
    }

    // Actualiza el texto escrito en el buscador sin disparar búsqueda inmediata
    fun onQueryTextoChange(nuevoTexto: String) {
        _uiState.update { it.copy(queryTexto = nuevoTexto) }
    }

    // Ejecuta la búsqueda al pulsar la lupa o la tecla de buscar en el teclado
    fun ejecutarBusqueda() {
        val actual = _uiState.value
        adminRepository.registrarBusqueda(
            termino = actual.queryTexto,
            region = actual.regionFiltro.ifBlank { null },
            rubro = actual.rubroFiltro.ifBlank { null }
        )
        cargarMercado()
    }

    // Filtra por región y limpia la comuna seleccionada si cambia
    fun onRegionFiltroChange(nuevaRegion: String) {
        val actual = _uiState.value
        val regionFinal = if (nuevaRegion == actual.regionFiltro) "" else nuevaRegion
        _uiState.update {
            it.copy(
                regionFiltro = regionFinal,
                comunaFiltro = ""
            )
        }
        adminRepository.registrarBusqueda(
            termino = actual.queryTexto,
            region = regionFinal.ifBlank { null },
            rubro = actual.rubroFiltro.ifBlank { null }
        )
        cargarMercado()
    }

    // Filtra por comuna dentro de la región activa
    fun onComunaFiltroChange(nuevaComuna: String) {
        val actual = _uiState.value
        val comunaFinal = if (nuevaComuna == actual.comunaFiltro) "" else nuevaComuna
        _uiState.update { it.copy(comunaFiltro = comunaFinal) }
        adminRepository.registrarBusqueda(
            termino = actual.queryTexto,
            region = actual.regionFiltro.ifBlank { null },
            rubro = actual.rubroFiltro.ifBlank { null }
        )
        cargarMercado()
    }

    // Filtra por rubro textil seleccionado
    fun onRubroFiltroChange(nuevoRubro: String) {
        val actual = _uiState.value
        val rubroFinal = if (nuevoRubro == actual.rubroFiltro) "" else nuevoRubro
        _uiState.update { it.copy(rubroFiltro = rubroFinal) }
        adminRepository.registrarBusqueda(
            termino = actual.queryTexto,
            region = actual.regionFiltro.ifBlank { null },
            rubro = rubroFinal.ifBlank { null }
        )
        cargarMercado()
    }

    // Restablece todos los filtros aplicados y recarga la lista completa
    fun limpiarFiltros() {
        _uiState.update {
            it.copy(
                queryTexto = "",
                regionFiltro = "",
                comunaFiltro = "",
                rubroFiltro = ""
            )
        }
        cargarMercado()
    }

    // Carga las emprendedoras que cumplen los filtros junto con su foto de portada
    private fun cargarMercado() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val actual = _uiState.value
            val rubroEnum = Rubro.entries.firstOrNull { it.nombre.equals(actual.rubroFiltro, ignoreCase = true) }

            val lista = emprendedoraRepository.obtenerMercado(
                textoBusqueda = actual.queryTexto,
                region = actual.regionFiltro.ifBlank { null },
                comuna = actual.comunaFiltro.ifBlank { null },
                rubro = rubroEnum
            ).first()

            val itemsConPortada = lista.map { emp ->
                val productos = productoRepository.obtenerPorEmprendedora(emp.id).first()
                val portada = productos.firstOrNull { it.fotos.isNotEmpty() }?.fotos?.firstOrNull()
                ItemMercadoEmprendedora(
                    emprendedora = emp,
                    fotoPortada = portada,
                    totalProductos = productos.size
                )
            }

            _uiState.update {
                it.copy(
                    emprendedoras = itemsConPortada,
                    cargando = false
                )
            }
        }
    }
}
