package com.example.textilcircular.educacion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.educacion.repository.EducacionRepository
import com.example.textilcircular.educacion.repository.EducacionRepositoryWikipedia
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel desacoplado para gestionar el contenido educativo de la API externa sin tocar Room ni otros ViewModels.
class EducacionViewModel(
    private val repository: EducacionRepository = EducacionRepositoryWikipedia()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EducacionUiState>(EducacionUiState.Cargando)
    val uiState: StateFlow<EducacionUiState> = _uiState.asStateFlow()

    init {
        cargarContenidoEducativo()
    }

    fun cargarContenidoEducativo() {
        _uiState.value = EducacionUiState.Cargando
        viewModelScope.launch {
            when (val resultado = repository.obtenerTemasEducativos()) {
                is Resultado.Exito -> {
                    _uiState.value = EducacionUiState.Exito(resultado.datos)
                }
                is Resultado.Error -> {
                    _uiState.value = EducacionUiState.Error(resultado.mensaje)
                }
            }
        }
    }

    fun reintentar() {
        cargarContenidoEducativo()
    }
}
