package com.example.textilcircular

import android.app.Application
import com.example.textilcircular.di.AppContainer
import com.example.textilcircular.di.ContenedorApp

// Clase de la aplicación. Se ejecuta al iniciar y prepara los datos compartidos.
class TextilCircularApp : Application() {

    // Contenedor que guarda los repositorios para que los usen los ViewModels.
    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
