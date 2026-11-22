package com.example.textilcircular.data.remote.dto

data class MensajeResponseDto(
    val id: Long,
    val conversacionId: Long,
    val emisor: String,
    val contenido: String,
    val fechaHora: String,
    val esLeido: Boolean,
    val productoId: Long? = null,
    val nombreProducto: String? = null
)
