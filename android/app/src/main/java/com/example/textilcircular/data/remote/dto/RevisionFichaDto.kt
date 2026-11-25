package com.example.textilcircular.data.remote.dto

// DTO para solicitar la revisión de una ficha de emprendedora (APROBAR, RECHAZAR, OCULTAR)
data class RevisionFichaRequestDto(
    val accion: String,
    val motivo: String? = null
)

// DTO con la respuesta de moderación tras actualizar el estado de la ficha
data class RevisionFichaResponseDto(
    val id: Long,
    val estadoFicha: String,
    val motivoRevision: String? = null,
    val fechaRevision: String? = null
)
