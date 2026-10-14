package com.example.textilcircular.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.textilcircular.TextilCircularApp
import com.example.textilcircular.ui.screens.auth.LoginViewModel
import com.example.textilcircular.ui.screens.auth.RegistroGuiadoViewModel
import com.example.textilcircular.ui.screens.catalog.CatalogoViewModel
import com.example.textilcircular.ui.screens.catalog.ProductoFormViewModel
import com.example.textilcircular.ui.screens.cliente.ExplorarViewModel
import com.example.textilcircular.ui.screens.cliente.MiPerfilClienteViewModel
import com.example.textilcircular.ui.screens.cliente.RegistroClienteViewModel
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
            RegistroClienteViewModel(app.contenedor.sesionRepository)
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            PerfilViewModel(
                sesionRepository = app.contenedor.sesionRepository,
                emprendedoraRepository = app.contenedor.emprendedoraRepository,
                sincronizacionRepository = app.contenedor.sincronizacionRepository
            )
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            CatalogoViewModel(
                productoRepository = app.contenedor.productoRepository,
                sesionRepository = app.contenedor.sesionRepository
            )
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            ProductoFormViewModel(
                productoRepository = app.contenedor.productoRepository,
                sesionRepository = app.contenedor.sesionRepository
            )
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            ExplorarViewModel(
                emprendedoraRepository = app.contenedor.emprendedoraRepository,
                productoRepository = app.contenedor.productoRepository,
                adminRepository = app.contenedor.adminRepository
            )
        }

        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp)
            MiPerfilClienteViewModel(
                sesionRepository = app.contenedor.sesionRepository,
                clienteRepository = app.contenedor.clienteRepository
            )
        }
    }
}
