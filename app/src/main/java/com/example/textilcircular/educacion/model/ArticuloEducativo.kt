package com.example.textilcircular.educacion.model

// Modelo de dominio limpio para presentar el contenido educativo en la interfaz gráfica.
data class ArticuloEducativo(
    val clave: String,
    val titulo: String,
    val extracto: String,
    val imagenUrl: String?,
    val urlOriginal: String,
    val etiquetaTematica: String,
    val fuente: String = "Wikipedia en español (CC BY-SA 4.0)"
)
