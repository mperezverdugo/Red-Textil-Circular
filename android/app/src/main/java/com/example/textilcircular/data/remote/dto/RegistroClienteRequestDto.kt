package com.example.textilcircular.data.remote.dto

data class RegistroClienteRequestDto(
    val nombre: String,
    val email: String,
    val password: String,
    val aceptaLeyDatos: Boolean
)
