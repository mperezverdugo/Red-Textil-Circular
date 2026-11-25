package com.example.textilcircular.data.remote.dto

// DTO para editar los datos comerciales de una ficha desde administración (PUT /admin/emprendedoras/{id}).
data class EditarFichaAdminRequestDto(
    val nombreComercial: String,
    val region: String,
    val comuna: String,
    val rubros: List<String>,
    val instagram: String? = null
)
