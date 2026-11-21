package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Cliente
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.Rol
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Estados posibles de la sesión de usuario en la aplicación.
sealed interface EstadoSesion {
    data object NoAutenticado : EstadoSesion
    data class AutenticadoEmprendedora(val emprendedora: Emprendedora) : EstadoSesion
    data class AutenticadoCliente(val cliente: Cliente) : EstadoSesion
    data object AutenticadoAdmin : EstadoSesion

    val usuarioId: Int?
        get() = when (this) {
            is AutenticadoEmprendedora -> emprendedora.id
            is AutenticadoCliente -> cliente.id
            else -> null
        }

    val rol: Rol?
        get() = when (this) {
            is AutenticadoEmprendedora -> Rol.EMPRENDEDORA
            is AutenticadoCliente -> Rol.CLIENTE
            is AutenticadoAdmin -> Rol.ADMINISTRADORA
            is NoAutenticado -> null
        }
}

// Repositorio para consultar y gestionar el inicio, cierre y estado de la sesión activa.
interface SesionRepository {
    val estadoSesion: StateFlow<EstadoSesion>

    suspend fun iniciarSesionEmprendedora(correo: String, clave: String): Resultado<Emprendedora>
    suspend fun iniciarSesionCliente(correo: String, clave: String): Resultado<Cliente>
    suspend fun iniciarSesionAdmin(correo: String, clave: String): Resultado<Unit>
    suspend fun registrarEmprendedora(emprendedora: Emprendedora, clave: String): Resultado<Emprendedora>
    suspend fun registrarCliente(nombre: String, correo: String, clave: String, aceptaDatos: Boolean): Resultado<Cliente>
    suspend fun iniciarSesionDemoEmprendedora(): Resultado<Emprendedora>
    suspend fun iniciarSesionDemoCliente(): Resultado<Cliente>
    suspend fun iniciarSesionDemoAdmin(): Resultado<Unit>
    suspend fun cerrarSesion()
    suspend fun actualizarEmprendedoraSesion(emprendedora: Emprendedora)
    suspend fun actualizarClienteSesion(cliente: Cliente)
}

// Implementación en memoria de la sesión mientras se conecta SQLite y el servidor.
class SesionRepositoryMemoria(
    private val emprendedoraRepository: EmprendedoraRepository,
    private val clienteRepository: ClienteRepository = ClienteRepositoryMemoria()
) : SesionRepository {

    private val _estadoSesion = MutableStateFlow<EstadoSesion>(EstadoSesion.NoAutenticado)
    override val estadoSesion: StateFlow<EstadoSesion> = _estadoSesion.asStateFlow()

    override suspend fun iniciarSesionEmprendedora(correo: String, clave: String): Resultado<Emprendedora> {
        val correoNormalizado = correo.trim().lowercase()
        // TODO: conectar con el servidor para validar credenciales y token
        val emprendedora = DatosDemo.EmprendimientosAdminDemo
            .map { it.emprendedora }
            .firstOrNull { it.email.lowercase() == correoNormalizado }
            ?: DatosDemo.EmprendedoraDemo

        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(emprendedora)
        return Resultado.Exito(emprendedora)
    }

    override suspend fun iniciarSesionCliente(correo: String, clave: String): Resultado<Cliente> {
        // Autenticar cliente validando hash y sal
        val resultadoAuth = clienteRepository.autenticar(correo, clave)
        return when (resultadoAuth) {
            is Resultado.Exito -> {
                _estadoSesion.value = EstadoSesion.AutenticadoCliente(resultadoAuth.datos)
                Resultado.Exito(resultadoAuth.datos)
            }
            is Resultado.Error -> resultadoAuth
        }
    }

    override suspend fun iniciarSesionAdmin(correo: String, clave: String): Resultado<Unit> {
        // TODO: conectar con el servidor para autenticación administrativa
        _estadoSesion.value = EstadoSesion.AutenticadoAdmin
        return Resultado.Exito(Unit)
    }

    override suspend fun registrarEmprendedora(emprendedora: Emprendedora, clave: String): Resultado<Emprendedora> {
        // TODO: registrar en SQLite y sincronizar con el servidor
        val resultadoGuardado = emprendedoraRepository.guardar(emprendedora)
        return when (resultadoGuardado) {
            is Resultado.Exito -> {
                _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(resultadoGuardado.datos)
                Resultado.Exito(resultadoGuardado.datos)
            }
            is Resultado.Error -> resultadoGuardado
        }
    }

    override suspend fun registrarCliente(
        nombre: String,
        correo: String,
        clave: String,
        aceptaDatos: Boolean
    ): Resultado<Cliente> {
        val resultadoRegistro = clienteRepository.registrar(
            nombre = nombre,
            email = correo,
            password = clave,
            aceptaLeyDatos = aceptaDatos
        )
        return when (resultadoRegistro) {
            is Resultado.Exito -> {
                _estadoSesion.value = EstadoSesion.AutenticadoCliente(resultadoRegistro.datos)
                Resultado.Exito(resultadoRegistro.datos)
            }
            is Resultado.Error -> resultadoRegistro
        }
    }

    override suspend fun iniciarSesionDemoEmprendedora(): Resultado<Emprendedora> {
        val demo = DatosDemo.EmprendedoraDemo
        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(demo)
        return Resultado.Exito(demo)
    }

    override suspend fun iniciarSesionDemoCliente(): Resultado<Cliente> {
        val demo = DatosDemo.ClienteDemo
        _estadoSesion.value = EstadoSesion.AutenticadoCliente(demo)
        return Resultado.Exito(demo)
    }

    override suspend fun iniciarSesionDemoAdmin(): Resultado<Unit> {
        _estadoSesion.value = EstadoSesion.AutenticadoAdmin
        return Resultado.Exito(Unit)
    }

    override suspend fun cerrarSesion() {
        _estadoSesion.value = EstadoSesion.NoAutenticado
    }

    override suspend fun actualizarEmprendedoraSesion(emprendedora: Emprendedora) {
        if (_estadoSesion.value is EstadoSesion.AutenticadoEmprendedora) {
            _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(emprendedora)
        }
    }

    override suspend fun actualizarClienteSesion(cliente: Cliente) {
        if (_estadoSesion.value is EstadoSesion.AutenticadoCliente) {
            _estadoSesion.value = EstadoSesion.AutenticadoCliente(cliente)
        }
    }
}
