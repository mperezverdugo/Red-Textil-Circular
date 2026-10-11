package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Estados posibles de la sesión de usuario en la aplicación.
sealed interface EstadoSesion {
    data object NoAutenticado : EstadoSesion
    data class AutenticadoEmprendedora(val emprendedora: Emprendedora) : EstadoSesion
    data object AutenticadoAdmin : EstadoSesion
}

// Repositorio para consultar y gestionar el inicio, cierre y estado de la sesión.
interface SesionRepository {
    val estadoSesion: StateFlow<EstadoSesion>

    suspend fun iniciarSesionEmprendedora(correo: String, clave: String): Resultado<Emprendedora>
    suspend fun iniciarSesionAdmin(correo: String, clave: String): Resultado<Unit>
    suspend fun registrarEmprendedora(emprendedora: Emprendedora, clave: String): Resultado<Emprendedora>
    suspend fun iniciarSesionDemoEmprendedora(): Resultado<Emprendedora>
    suspend fun iniciarSesionDemoAdmin(): Resultado<Unit>
    suspend fun cerrarSesion()
    suspend fun actualizarEmprendedoraSesion(emprendedora: Emprendedora)
}

// Implementación en memoria de la sesión mientras se conecta SQLite y el servidor.
class SesionRepositoryMemoria(
    private val emprendedoraRepository: EmprendedoraRepository
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

    override suspend fun iniciarSesionDemoEmprendedora(): Resultado<Emprendedora> {
        val demo = DatosDemo.EmprendedoraDemo
        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(demo)
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
}
