package com.example.textilcircular.model

// Datos de un cliente registrado para consultar productos y contactar emprendedoras.
// TODO: confirmar con la Fundación si el cliente necesita cuenta para escribir.
data class Cliente(
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val passwordHash: String = "",
    val sal: String = "",
    val aceptaLeyDatos: Boolean = true
)
