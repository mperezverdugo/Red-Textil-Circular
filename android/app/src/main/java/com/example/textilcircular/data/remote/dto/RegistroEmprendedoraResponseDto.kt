package com.example.textilcircular.data.remote.dto

data class RegistroEmprendedoraResponseDto(
    val usuario: UsuarioDto,
    val emprendedoraId: Long,
    val estadoFicha: String
)
