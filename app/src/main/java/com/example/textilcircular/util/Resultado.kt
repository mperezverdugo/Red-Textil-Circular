package com.example.textilcircular.util

// Representa el resultado de una operación para no lanzar excepciones hacia la interfaz.
sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String, val excepcion: Throwable? = null) : Resultado<Nothing>()
}
