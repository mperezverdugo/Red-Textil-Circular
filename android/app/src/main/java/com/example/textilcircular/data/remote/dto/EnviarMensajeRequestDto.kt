package com.example.textilcircular.data.remote.dto

data class EnviarMensajeRequestDto(
    val contenido: String,
    val productoId: Long? = null,
    val nombreProducto: String? = null
)
