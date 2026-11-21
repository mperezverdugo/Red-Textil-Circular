package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.model.Cliente
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.Rol
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Implementación del repositorio de sesión que persiste el usuario activo en SQLite.
class SesionRepositoryRoom(
    private val sesionDao: SesionDao,
    private val emprendedoraDao: EmprendedoraDao,
    private val clienteDao: ClienteDao,
    private val emprendedoraRepository: EmprendedoraRepository,
    private val clienteRepository: ClienteRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SesionRepository {

    private val _estadoSesion = MutableStateFlow<EstadoSesion>(EstadoSesion.NoAutenticado)
    override val estadoSesion: StateFlow<EstadoSesion> = _estadoSesion.asStateFlow()

    init {
        // Al instanciar el repositorio, intentamos restaurar la sesión que estaba guardada en SQLite
        scope.launch {
            restaurarSesionDesdeBaseDeDatos()
        }
    }

    // Lee la tabla sesion_activa y recarga el perfil correspondiente
    internal suspend fun restaurarSesionDesdeBaseDeDatos() {
        try {
            val sesionGuardada = sesionDao.obtenerSesionDirecta() ?: return
            when (sesionGuardada.rol) {
                Rol.EMPRENDEDORA -> {
                    val entidad = sesionGuardada.usuarioId?.let { emprendedoraDao.obtenerPorIdDirecto(it) }
                    if (entidad != null) {
                        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(entidad.aModelo())
                    } else {
                        sesionDao.cerrarSesion()
                        _estadoSesion.value = EstadoSesion.NoAutenticado
                    }
                }
                Rol.CLIENTE -> {
                    val entidad = sesionGuardada.usuarioId?.let { clienteDao.obtenerPorIdDirecto(it) }
                    if (entidad != null) {
                        _estadoSesion.value = EstadoSesion.AutenticadoCliente(entidad.aModelo())
                    } else {
                        sesionDao.cerrarSesion()
                        _estadoSesion.value = EstadoSesion.NoAutenticado
                    }
                }
                Rol.ADMINISTRADORA -> {
                    _estadoSesion.value = EstadoSesion.AutenticadoAdmin
                }
            }
        } catch (_: Exception) {
            _estadoSesion.value = EstadoSesion.NoAutenticado
        }
    }

    override suspend fun iniciarSesionEmprendedora(correo: String, clave: String): Resultado<Emprendedora> = withContext(Dispatchers.IO) {
        val correoNormalizado = correo.trim().lowercase()

        // Buscamos la artesana en SQLite por su correo electrónico
        val entidad = emprendedoraDao.obtenerPorEmailDirecto(correoNormalizado)
            ?: return@withContext Resultado.Error("No existe una cuenta registrada con este correo")

        val emprendedora = entidad.aModelo()

        // Persistimos en SQLite para recordar la sesión
        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.EMPRENDEDORA,
                usuarioId = emprendedora.id,
                correo = emprendedora.email
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(emprendedora)

        // TODO: cuando haya servidor, validar credenciales con token JWT en la API REST
        Resultado.Exito(emprendedora)
    }

    override suspend fun iniciarSesionCliente(correo: String, clave: String): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val resultadoAuth = clienteRepository.autenticar(correo, clave)
        when (resultadoAuth) {
            is Resultado.Exito -> {
                val cliente = resultadoAuth.datos
                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.CLIENTE,
                        usuarioId = cliente.id,
                        correo = cliente.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoCliente(cliente)
                Resultado.Exito(cliente)
            }
            is Resultado.Error -> resultadoAuth
        }
    }

    override suspend fun iniciarSesionAdmin(correo: String, clave: String): Resultado<Unit> = withContext(Dispatchers.IO) {
        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.ADMINISTRADORA,
                usuarioId = null,
                correo = correo.trim()
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoAdmin

        // TODO: cuando haya servidor, autenticar rol administrativo con la API REST
        Resultado.Exito(Unit)
    }

    override suspend fun registrarEmprendedora(emprendedora: Emprendedora, clave: String): Resultado<Emprendedora> = withContext(Dispatchers.IO) {
        val guardado = emprendedoraRepository.guardar(emprendedora)
        when (guardado) {
            is Resultado.Exito -> {
                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.EMPRENDEDORA,
                        usuarioId = guardado.datos.id,
                        correo = guardado.datos.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(guardado.datos)
                Resultado.Exito(guardado.datos)
            }
            is Resultado.Error -> guardado
        }
    }

    override suspend fun registrarCliente(
        nombre: String,
        correo: String,
        clave: String,
        aceptaDatos: Boolean
    ): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val registro = clienteRepository.registrar(nombre, correo, clave, aceptaDatos)
        when (registro) {
            is Resultado.Exito -> {
                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.CLIENTE,
                        usuarioId = registro.datos.id,
                        correo = registro.datos.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoCliente(registro.datos)
                Resultado.Exito(registro.datos)
            }
            is Resultado.Error -> registro
        }
    }

    override suspend fun iniciarSesionDemoEmprendedora(): Resultado<Emprendedora> = withContext(Dispatchers.IO) {
        val demo = emprendedoraDao.obtenerPorIdDirecto(1)?.aModelo() ?: DatosDemo.EmprendedoraDemo
        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.EMPRENDEDORA,
                usuarioId = demo.id,
                correo = demo.email
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(demo)
        Resultado.Exito(demo)
    }

    override suspend fun iniciarSesionDemoCliente(): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val demo = clienteDao.obtenerPorIdDirecto(1)?.aModelo() ?: DatosDemo.ClienteDemo
        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.CLIENTE,
                usuarioId = demo.id,
                correo = demo.email
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoCliente(demo)
        Resultado.Exito(demo)
    }

    override suspend fun iniciarSesionDemoAdmin(): Resultado<Unit> = withContext(Dispatchers.IO) {
        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.ADMINISTRADORA,
                usuarioId = null,
                correo = "admin@textilcircular.cl"
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoAdmin
        Resultado.Exito(Unit)
    }

    // Cierra la sesión activa borrando el registro de SQLite
    override suspend fun cerrarSesion() = withContext(Dispatchers.IO) {
        sesionDao.cerrarSesion()
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
