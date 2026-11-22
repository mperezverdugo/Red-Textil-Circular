package com.example.textilcircular.data.remote.dto

data class ConversacionResponseDto(
    val id: Long,
    val clienteId: Long,
    val emprendedoraId: Long,
    val productoId: Long? = null,
    val nombreCliente: String,
    val nombreEmprendimiento: String,
    val ultimoMensaje: String,
    val fechaHora: String,
    val noLeidosCliente: Int,
    val noLeidosEmprendedora: Int
)
