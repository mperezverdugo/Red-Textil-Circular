package com.example.textilcircular.data.remote.dto

data class ProductoResponseDto(
    val id: Long,
    val emprendedoraId: Long,
    val nombre: String,
    val descripcionCorta: String,
    val explicacionProceso: String? = null
)
