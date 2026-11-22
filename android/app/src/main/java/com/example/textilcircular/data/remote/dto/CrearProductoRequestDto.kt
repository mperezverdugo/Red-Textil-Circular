package com.example.textilcircular.data.remote.dto

data class CrearProductoRequestDto(
    val nombre: String,
    val descripcionCorta: String,
    val explicacionProceso: String? = null
)
