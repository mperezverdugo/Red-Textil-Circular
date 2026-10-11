package com.example.textilcircular.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.textilcircular.TextilCircularApp
import com.example.textilcircular.ui.screens.auth.LoginViewModel
import com.example.textilcircular.ui.screens.auth.RegistroGuiadoViewModel
import com.example.textilcircular.ui.screens.profile.PerfilViewModel

// Fábrica proveedora de ViewModels con inyección manual de dependencias desde AppContainer.
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            LoginViewModel(app.contenedor.sesionRepository)
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            RegistroGuiadoViewModel(app.contenedor.sesionRepository)
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            PerfilViewModel(
                sesionRepository = app.contenedor.sesionRepository,
                emprendedoraRepository = app.contenedor.emprendedoraRepository,
                sincronizacionRepository = app.contenedor.sincronizacionRepository
            )
        }
    }
}
