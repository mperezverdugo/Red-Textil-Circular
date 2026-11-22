package com.example.textilcircular.data.remote.dto

data class EmprendedoraPublicaDto(
    val id: Long,
    val usuarioId: Long,
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<String>,
    val instagram: String? = null
)
