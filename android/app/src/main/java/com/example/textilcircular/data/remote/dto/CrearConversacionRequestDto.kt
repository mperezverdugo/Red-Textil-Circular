package com.example.textilcircular.data.remote.dto

data class CrearConversacionRequestDto(
    val emprendedoraId: Long,
    val nombreCliente: String,
    val nombreEmprendimiento: String,
    val productoId: Long? = null
)
