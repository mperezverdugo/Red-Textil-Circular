package com.example.textilcircular.model

/**
 * Modelo de datos de una Emprendedora registrada en el Directorio Textil Circular.
 *
 * Requisitos asociados: RF-01 a RF-06 (Registro), RF-09 (Ficha y Perfil), RF-27 (Administración).
 * Cumple con Ley N.º 21.719 y Ley N.º 19.628 para la protección de datos personales.
 */
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
