package com.example.textilcircular.di

import android.content.Context
import com.example.textilcircular.data.repository.AdminRepository
import com.example.textilcircular.data.repository.AdminRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepository
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.MensajeRepository
import com.example.textilcircular.data.repository.MensajeRepositoryMemoria
import com.example.textilcircular.data.repository.ProductoRepository
import com.example.textilcircular.data.repository.ProductoRepositoryMemoria
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.data.repository.SincronizacionRepository
import com.example.textilcircular.data.repository.SincronizacionRepositoryMemoria

// Contenedor de dependencias manual que centraliza los repositorios para toda la aplicación.
interface AppContainer {
    val sesionRepository: SesionRepository
    val emprendedoraRepository: EmprendedoraRepository
    val productoRepository: ProductoRepository
    val mensajeRepository: MensajeRepository
    val sincronizacionRepository: SincronizacionRepository
    val adminRepository: AdminRepository
}

// Implementación que crea y mantiene instancias únicas de los repositorios en memoria.
class ContenedorApp(private val context: Context) : AppContainer {

    override val emprendedoraRepository: EmprendedoraRepository by lazy {
        EmprendedoraRepositoryMemoria()
    }

    override val sesionRepository: SesionRepository by lazy {
        SesionRepositoryMemoria(emprendedoraRepository)
    }

    override val productoRepository: ProductoRepository by lazy {
        ProductoRepositoryMemoria()
    }

    override val mensajeRepository: MensajeRepository by lazy {
        MensajeRepositoryMemoria()
    }

    override val sincronizacionRepository: SincronizacionRepository by lazy {
        SincronizacionRepositoryMemoria()
    }

    override val adminRepository: AdminRepository by lazy {
        AdminRepositoryMemoria(emprendedoraRepository)
    }
}
