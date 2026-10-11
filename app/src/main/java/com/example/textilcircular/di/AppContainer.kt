package com.example.textilcircular.di

import android.content.Context

// Contenedor de dependencias manual. Aquí se centralizan los repositorios para que las pantallas no los creen por su cuenta.
interface AppContainer {
    // Los repositorios se agregarán aquí para compartirlos en toda la aplicación.
}

// Implementación que crea y mantiene una sola copia de cada repositorio en memoria.
class ContenedorApp(private val context: Context) : AppContainer {
    // Aquí se inicializarán los repositorios en memoria durante la siguiente etapa.
}
