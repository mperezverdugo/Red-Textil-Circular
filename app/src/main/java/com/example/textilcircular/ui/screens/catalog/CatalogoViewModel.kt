package com.example.textilcircular.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.ProductoRepository
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de catálogo de productos.
data class CatalogoUiState(
    val productos: List<Producto> = emptyList(),
    val estaCargando: Boolean = false,
    val error: String? = null
)

// ViewModel encargado de cargar y observar las creaciones textiles de la emprendedora activa.
class CatalogoViewModel(
    private val productoRepository: ProductoRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState(estaCargando = true))
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        observarCatalogo()
    }

    private fun observarCatalogo() {
        viewModelScope.launch {
            sesionRepository.estadoSesion.collect { estadoSesion ->
                val emprendedoraId = when (estadoSesion) {
                    is EstadoSesion.AutenticadoEmprendedora -> estadoSesion.emprendedora.id
                    else -> 1 // ID por defecto para pruebas
                }

                productoRepository.obtenerPorEmprendedora(emprendedoraId).collect { lista ->
                    _uiState.update {
                        it.copy(
                            productos = lista,
                            estaCargando = false,
                            error = null
                        )
                    }
                }
            }
        }
    }
}
