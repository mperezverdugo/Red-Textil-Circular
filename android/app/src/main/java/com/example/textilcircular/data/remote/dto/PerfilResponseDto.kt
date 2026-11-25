package com.example.textilcircular.data.remote.dto

// DTO devuelto por GET /usuarios/me en ms-usuarios.
data class PerfilResponseDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String,
    val aceptaLeyDatos: Boolean,
    val ficha: FichaPerfilDto? = null
)

data class FichaPerfilDto(
    val id: Long,
    val rut: String,
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<String>,
    val instagram: String? = null,
    val activa: Boolean,
    val estadoFicha: String,
    val motivoRevision: String? = null,
    val fechaRevision: String? = null
)
