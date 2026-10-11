package com.example.textilcircular.model

// Datos de una emprendedora registrada en el directorio.
data class Emprendedora(
    val id: Int = 0,
    val rut: String,
    val nombre: String,
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<Rubro>,
    val email: String,
    val instagram: String? = null,
    val activa: Boolean = true
)
