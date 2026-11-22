package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.UsuariosApi
import com.example.textilcircular.data.remote.dto.LoginRequestDto
import com.example.textilcircular.data.remote.dto.RegistroClienteRequestDto
import com.example.textilcircular.data.remote.dto.RegistroEmprendedoraRequestDto
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
import com.example.textilcircular.data.local.entity.ClienteEntity
// Implementación del repositorio de sesión que persiste en SQLite y autentica con ms-usuarios vía Retrofit.
class SesionRepositoryRoom(
    private val sesionDao: SesionDao,
    private val emprendedoraDao: EmprendedoraDao,
    private val clienteDao: ClienteDao,
    private val emprendedoraRepository: EmprendedoraRepository,
    private val clienteRepository: ClienteRepository,
    private val usuariosApi: UsuariosApi = ClienteRetrofit.usuariosApi,
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

        // 1. Intentamos autenticar con ms-usuarios vía Retrofit
        try {
            val respuesta = usuariosApi.login(LoginRequestDto(correoNormalizado, clave))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val loginBody = respuesta.body()!!
                ClienteRetrofit.tokenActivo = loginBody.token

                val local = emprendedoraDao.obtenerPorEmailDirecto(correoNormalizado)?.aModelo()
                val emprendedora = local?.copy(usuarioId = loginBody.usuario.id.toInt())
                    ?: Emprendedora(
                        id = (loginBody.usuario.id - 1).toInt(),
                        rut = "",
                        nombre = loginBody.usuario.nombre,
                        nombreComercial = loginBody.usuario.nombre,
                        region = "",
                        comuna = "",
                        rubros = emptyList(),
                        email = loginBody.usuario.email,
                        activa = true,
                        usuarioId = loginBody.usuario.id.toInt()
                    )

                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.EMPRENDEDORA,
                        usuarioId = emprendedora.id,
                        correo = emprendedora.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(emprendedora)
                return@withContext Resultado.Exito(emprendedora)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                return@withContext Resultado.Error(errorMsg)
            }
        } catch (_: java.io.IOException) {
            // Falla de red: continúa con el almacenamiento local (offline fallback)
        }

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
        Resultado.Exito(emprendedora)
    }

    override suspend fun iniciarSesionCliente(correo: String, clave: String): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val correoNormalizado = correo.trim().lowercase()

        try {
            val respuesta = usuariosApi.login(LoginRequestDto(correoNormalizado, clave))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val loginBody = respuesta.body()!!
                ClienteRetrofit.tokenActivo = loginBody.token

                val local = clienteDao.obtenerPorEmailDirecto(correoNormalizado)?.aModelo()
                val cliente = (local ?: Cliente(
                    id = loginBody.usuario.id.toInt(),
                    nombre = loginBody.usuario.nombre,
                    email = loginBody.usuario.email,
                    passwordHash = "",
                    sal = "",
                    aceptaLeyDatos = true
                )).copy(id = loginBody.usuario.id.toInt())

                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.CLIENTE,
                        usuarioId = cliente.id,
                        correo = cliente.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoCliente(cliente)
                return@withContext Resultado.Exito(cliente)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                return@withContext Resultado.Error(errorMsg)
            }
        } catch (_: java.io.IOException) {
            // Falla de red: continúa con autenticación local
        }

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
        val correoLimpio = correo.trim().lowercase()

        try {
            val respuesta = usuariosApi.login(LoginRequestDto(correoLimpio, clave))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val loginBody = respuesta.body()!!
                ClienteRetrofit.tokenActivo = loginBody.token

                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.ADMINISTRADORA,
                        usuarioId = loginBody.usuario.id.toInt(),
                        correo = loginBody.usuario.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoAdmin
                return@withContext Resultado.Exito(Unit)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                return@withContext Resultado.Error(errorMsg)
            }
        } catch (_: java.io.IOException) {
            // Falla de red: fallback local
        }

        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.ADMINISTRADORA,
                usuarioId = null,
                correo = correo.trim()
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoAdmin
        Resultado.Exito(Unit)
    }

    override suspend fun registrarEmprendedora(emprendedora: Emprendedora, clave: String): Resultado<Emprendedora> = withContext(Dispatchers.IO) {
        try {
            val respuesta = usuariosApi.registrarEmprendedora(
                RegistroEmprendedoraRequestDto(
                    nombre = emprendedora.nombre,
                    email = emprendedora.email.trim().lowercase(),
                    password = clave,
                    aceptaLeyDatos = true,
                    rut = emprendedora.rut,
                    nombreComercial = emprendedora.nombreComercial,
                    region = emprendedora.region,
                    comuna = emprendedora.comuna,
                    rubros = emprendedora.rubros.map { it.name },
                    instagram = emprendedora.instagram
                )
            )
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val respDto = respuesta.body()!!
                val guardada = emprendedora.copy(
                    id = respDto.emprendedoraId.toInt(),
                    usuarioId = respDto.usuario.id.toInt()
                )
                emprendedoraRepository.guardar(guardada)
                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.EMPRENDEDORA,
                        usuarioId = guardada.id,
                        correo = guardada.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(guardada)
                return@withContext Resultado.Exito(guardada)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                return@withContext Resultado.Error(errorMsg)
            }
        } catch (_: java.io.IOException) {
            // Falla de red: fallback local
        }

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
        val correoLimpio = correo.trim().lowercase()

        try {
            val respuesta = usuariosApi.registrarCliente(
                RegistroClienteRequestDto(
                    nombre = nombre.trim(),
                    email = correoLimpio,
                    password = clave,
                    aceptaLeyDatos = aceptaDatos
                )
            )
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val usuarioDto = respuesta.body()!!
                val cliente = Cliente(
                    id = usuarioDto.id.toInt(),
                    nombre = usuarioDto.nombre,
                    email = usuarioDto.email,
                    passwordHash = "",
                    sal = "",
                    aceptaLeyDatos = aceptaDatos
                )
                try {
                    clienteDao.insertar(ClienteEntity.desdeModelo(cliente))
                } catch (_: Exception) {}
                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.CLIENTE,
                        usuarioId = cliente.id,
                        correo = cliente.email
                    )
                )
                _estadoSesion.value = EstadoSesion.AutenticadoCliente(cliente)
                return@withContext Resultado.Exito(cliente)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                return@withContext Resultado.Error(errorMsg)
            }
        } catch (_: java.io.IOException) {
            // Falla de red: fallback local
        }

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

    // Cierra la sesión activa borrando el registro de SQLite y limpiando el token en memoria
    override suspend fun cerrarSesion() = withContext(Dispatchers.IO) {
        ClienteRetrofit.tokenActivo = null
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
