package com.example.textilcircular.di

import android.content.Context
import com.example.textilcircular.data.local.database.TextilCircularDatabase
import com.example.textilcircular.data.repository.AdminRepository
import com.example.textilcircular.data.repository.AdminRepositoryRoom
import com.example.textilcircular.data.repository.ClienteRepository
import com.example.textilcircular.data.repository.ClienteRepositoryRoom
import com.example.textilcircular.data.repository.EmprendedoraRepository
import com.example.textilcircular.data.repository.EmprendedoraRepositoryRoom
import com.example.textilcircular.data.repository.MensajeRepository
import com.example.textilcircular.data.repository.MensajeRepositoryRoom
import com.example.textilcircular.data.repository.ProductoRepository
import com.example.textilcircular.data.repository.ProductoRepositoryRoom
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.data.repository.SesionRepositoryRoom
import com.example.textilcircular.data.repository.SincronizacionRepository
import com.example.textilcircular.data.repository.SincronizacionRepositoryRoom

// Contenedor de dependencias manual que centraliza los repositorios para toda la aplicación.
interface AppContainer {
    val sesionRepository: SesionRepository
    val emprendedoraRepository: EmprendedoraRepository
    val clienteRepository: ClienteRepository
    val productoRepository: ProductoRepository
    val mensajeRepository: MensajeRepository
    val sincronizacionRepository: SincronizacionRepository
    val adminRepository: AdminRepository
}

// Implementación que crea la base de datos SQLite y mantiene instancias únicas de los repositorios Room.
class ContenedorApp(private val context: Context) : AppContainer {

    // Base de datos local SQLite administrada por Room
    private val database: TextilCircularDatabase by lazy {
        TextilCircularDatabase.obtenerInstancia(context)
    }

    override val clienteRepository: ClienteRepository by lazy {
        ClienteRepositoryRoom(database.clienteDao())
    }

    override val emprendedoraRepository: EmprendedoraRepository by lazy {
        EmprendedoraRepositoryRoom(database.emprendedoraDao())
    }

    override val sesionRepository: SesionRepository by lazy {
        SesionRepositoryRoom(
            sesionDao = database.sesionDao(),
            emprendedoraDao = database.emprendedoraDao(),
            clienteDao = database.clienteDao(),
            emprendedoraRepository = emprendedoraRepository,
            clienteRepository = clienteRepository
        )
    }

    override val productoRepository: ProductoRepository by lazy {
        ProductoRepositoryRoom(database.productoDao())
    }

    override val mensajeRepository: MensajeRepository by lazy {
        MensajeRepositoryRoom(
            mensajeDao = database.mensajeDao(),
            productoDao = database.productoDao()
        )
    }

    override val sincronizacionRepository: SincronizacionRepository by lazy {
        SincronizacionRepositoryRoom(database.sincronizacionDao())
    }

    override val adminRepository: AdminRepository by lazy {
        AdminRepositoryRoom(
            adminDao = database.adminDao(),
            emprendedoraDao = database.emprendedoraDao()
        )
    }
}

