package com.example.textilcircular.educacion.ui

import com.example.textilcircular.educacion.model.ArticuloEducativo

// Estados de la interfaz gráfica para el contenido educativo de la API externa.
sealed interface EducacionUiState {
    data object Cargando : EducacionUiState
    data class Exito(val articulos: List<ArticuloEducativo>) : EducacionUiState
    data class Error(val mensaje: String) : EducacionUiState
}
