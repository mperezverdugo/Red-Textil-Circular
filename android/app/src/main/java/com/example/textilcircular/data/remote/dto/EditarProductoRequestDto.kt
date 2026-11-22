package com.example.textilcircular.data.remote.dto

data class EditarProductoRequestDto(
    val nombre: String,
    val descripcionCorta: String,
    val explicacionProceso: String? = null
)
