package com.example.textilcircular.data.remote.dto

data class RegistroEmprendedoraRequestDto(
    val nombre: String,
    val email: String,
    val password: String,
    val aceptaLeyDatos: Boolean,
    val rut: String,
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<String>,
    val instagram: String? = null
)
