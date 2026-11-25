package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.UsuariosApi
import com.example.textilcircular.data.remote.dto.LoginRequestDto
import com.example.textilcircular.data.remote.dto.RegistroClienteRequestDto
import com.example.textilcircular.data.remote.dto.RegistroEmprendedoraRequestDto
import com.example.textilcircular.model.Cliente
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
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
            ClienteRetrofit.tokenActivo = sesionGuardada.token
            when (sesionGuardada.rol) {
                Rol.EMPRENDEDORA -> {
                    val entidad = sesionGuardada.emprendedoraId?.let { emprendedoraDao.obtenerPorIdDirecto(it) }
                        ?: sesionGuardada.usuarioId?.let { emprendedoraDao.obtenerPorUsuarioIdDirecto(it) }
                        ?: sesionGuardada.usuarioId?.let { emprendedoraDao.obtenerPorIdDirecto(it) }
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

    private suspend fun sincronizarPerfilEmprendedora(token: String, usuario: com.example.textilcircular.data.remote.dto.UsuarioDto): Emprendedora {
        val correoNormalizado = usuario.email.trim().lowercase()
        val local = emprendedoraDao.obtenerPorEmailDirecto(correoNormalizado)?.aModelo()
        try {
            val resPerfil = usuariosApi.obtenerMiPerfil("Bearer $token")
            if (resPerfil.isSuccessful && resPerfil.body() != null) {
                val perfil = resPerfil.body()!!
                val ficha = perfil.ficha
                if (ficha != null) {
                    val estadoFicha = try {
                        EstadoFichaEmprendimiento.valueOf(ficha.estadoFicha)
                    } catch (_: Exception) {
                        EstadoFichaEmprendimiento.ACTIVA
                    }
                    val rubrosParsed = ficha.rubros.mapNotNull { nombreRubro ->
                        try {
                            com.example.textilcircular.model.Rubro.valueOf(nombreRubro.trim().uppercase())
                        } catch (_: Exception) {
                            null
                        }
                    }
                    val empRemota = Emprendedora(
                        id = ficha.id.toInt(),
                        rut = ficha.rut,
                        nombre = perfil.nombre,
                        nombreComercial = ficha.nombreComercial,
                        region = ficha.region,
                        comuna = ficha.comuna,
                        rubros = rubrosParsed,
                        email = perfil.email,
                        instagram = ficha.instagram,
                        activa = ficha.activa,
                        estadoFicha = estadoFicha,
                        usuarioId = perfil.id.toInt()
                    )
                    try {
                        emprendedoraDao.insertar(EmprendedoraEntity.desdeModelo(empRemota))
                    } catch (_: Exception) {}
                    return empRemota
                }
            }
        } catch (_: Exception) {}

        val empFallback = local?.copy(usuarioId = usuario.id.toInt())
            ?: Emprendedora(
                id = (usuario.id - 1).toInt().coerceAtLeast(1),
                rut = "",
                nombre = usuario.nombre,
                nombreComercial = usuario.nombre,
                region = "",
                comuna = "",
                rubros = emptyList(),
                email = usuario.email,
                activa = true,
                estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
                usuarioId = usuario.id.toInt()
            )
        try {
            emprendedoraDao.insertar(EmprendedoraEntity.desdeModelo(empFallback))
        } catch (_: Exception) {}
        return empFallback
    }

    override suspend fun iniciarSesion(correo: String, clave: String): Resultado<Rol> = withContext(Dispatchers.IO) {
        val correoNormalizado = correo.trim().lowercase()
        try {
            val respuesta = usuariosApi.login(LoginRequestDto(correoNormalizado, clave))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val loginBody = respuesta.body()!!
                ClienteRetrofit.tokenActivo = loginBody.token
                val rolServidor = loginBody.usuario.rol.trim().uppercase()

                when (rolServidor) {
                    "ADMIN", "ADMINISTRADORA" -> {
                        sesionDao.guardarSesion(
                            SesionActivaEntity(
                                id = 1,
                                rol = Rol.ADMINISTRADORA,
                                usuarioId = loginBody.usuario.id.toInt(),
                                correo = loginBody.usuario.email,
                                token = loginBody.token
                            )
                        )
                        _estadoSesion.value = EstadoSesion.AutenticadoAdmin
                        return@withContext Resultado.Exito(Rol.ADMINISTRADORA)
                    }
                    "EMPRENDEDORA" -> {
                        val emp = sincronizarPerfilEmprendedora(loginBody.token, loginBody.usuario)
                        sesionDao.guardarSesion(
                            SesionActivaEntity(
                                id = 1,
                                rol = Rol.EMPRENDEDORA,
                                usuarioId = emp.usuarioId,
                                correo = emp.email,
                                token = loginBody.token,
                                emprendedoraId = emp.id
                            )
                        )
                        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(emp)
                        return@withContext Resultado.Exito(Rol.EMPRENDEDORA)
                    }
                    "CLIENTE" -> {
                        val local = clienteDao.obtenerPorEmailDirecto(correoNormalizado)?.aModelo()
                        val cliente = (local ?: Cliente(
                            id = loginBody.usuario.id.toInt(),
                            nombre = loginBody.usuario.nombre,
                            email = loginBody.usuario.email,
                            passwordHash = "",
                            sal = "",
                            aceptaLeyDatos = true
                        )).copy(id = loginBody.usuario.id.toInt())

                        try {
                            clienteDao.insertar(ClienteEntity.desdeModelo(cliente))
                        } catch (_: Exception) {}

                        sesionDao.guardarSesion(
                            SesionActivaEntity(
                                id = 1,
                                rol = Rol.CLIENTE,
                                usuarioId = cliente.id,
                                correo = cliente.email,
                                token = loginBody.token
                            )
                        )
                        _estadoSesion.value = EstadoSesion.AutenticadoCliente(cliente)
                        return@withContext Resultado.Exito(Rol.CLIENTE)
                    }
                    else -> {
                        return@withContext Resultado.Error("Rol desconocido devuelto por el servidor: $rolServidor")
                    }
                }
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                return@withContext Resultado.Error(errorMsg)
            }
        } catch (_: java.io.IOException) {
            // Falla de red: continúa con el almacenamiento local (offline fallback)
        }

        // 2. Intento de autenticación local
        val entidadEmp = emprendedoraDao.obtenerPorEmailDirecto(correoNormalizado)
        if (entidadEmp != null) {
            val emp = entidadEmp.aModelo()
            sesionDao.guardarSesion(
                SesionActivaEntity(
                    id = 1,
                    rol = Rol.EMPRENDEDORA,
                    usuarioId = emp.usuarioId,
                    correo = emp.email,
                    emprendedoraId = emp.id
                )
            )
            _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(emp)
            return@withContext Resultado.Exito(Rol.EMPRENDEDORA)
        }

        val resultadoAuth = clienteRepository.autenticar(correo, clave)
        if (resultadoAuth is Resultado.Exito) {
            val cli = resultadoAuth.datos
            sesionDao.guardarSesion(
                SesionActivaEntity(
                    id = 1,
                    rol = Rol.CLIENTE,
                    usuarioId = cli.id,
                    correo = cli.email
                )
            )
            _estadoSesion.value = EstadoSesion.AutenticadoCliente(cli)
            return@withContext Resultado.Exito(Rol.CLIENTE)
        }

        if (correoNormalizado.contains("admin")) {
            sesionDao.guardarSesion(
                SesionActivaEntity(
                    id = 1,
                    rol = Rol.ADMINISTRADORA,
                    usuarioId = null,
                    correo = correoNormalizado
                )
            )
            _estadoSesion.value = EstadoSesion.AutenticadoAdmin
            return@withContext Resultado.Exito(Rol.ADMINISTRADORA)
        }

        Resultado.Error("No existe una cuenta registrada con estas credenciales")
    }

    override suspend fun iniciarSesionEmprendedora(correo: String, clave: String): Resultado<Emprendedora> = withContext(Dispatchers.IO) {
        val correoNormalizado = correo.trim().lowercase()

        // 1. Intentamos autenticar con ms-usuarios vía Retrofit
        try {
            val respuesta = usuariosApi.login(LoginRequestDto(correoNormalizado, clave))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val loginBody = respuesta.body()!!
                val rolServidor = loginBody.usuario.rol.trim().uppercase()
                if (rolServidor != "EMPRENDEDORA") {
                    return@withContext Resultado.Error("El usuario autenticado no tiene rol de emprendedora ($rolServidor)")
                }
                ClienteRetrofit.tokenActivo = loginBody.token

                val emprendedora = sincronizarPerfilEmprendedora(loginBody.token, loginBody.usuario)

                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.EMPRENDEDORA,
                        usuarioId = emprendedora.usuarioId,
                        correo = emprendedora.email,
                        token = loginBody.token,
                        emprendedoraId = emprendedora.id
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
                usuarioId = emprendedora.usuarioId,
                correo = emprendedora.email,
                emprendedoraId = emprendedora.id
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
                val rolServidor = loginBody.usuario.rol.trim().uppercase()
                if (rolServidor != "CLIENTE") {
                    return@withContext Resultado.Error("El usuario autenticado no tiene rol de cliente ($rolServidor)")
                }
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

                try {
                    clienteDao.insertar(ClienteEntity.desdeModelo(cliente))
                } catch (_: Exception) {}

                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.CLIENTE,
                        usuarioId = cliente.id,
                        correo = cliente.email,
                        token = loginBody.token
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
                val rolServidor = loginBody.usuario.rol.trim().uppercase()
                if (rolServidor != "ADMIN" && rolServidor != "ADMINISTRADORA") {
                    return@withContext Resultado.Error("El usuario autenticado no tiene rol de administradora ($rolServidor)")
                }
                ClienteRetrofit.tokenActivo = loginBody.token

                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.ADMINISTRADORA,
                        usuarioId = loginBody.usuario.id.toInt(),
                        correo = loginBody.usuario.email,
                        token = loginBody.token
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
                val estadoConfirmado = try {
                    EstadoFichaEmprendimiento.valueOf(respDto.estadoFicha)
                } catch (_: Exception) {
                    EstadoFichaEmprendimiento.POR_REVISAR
                }
                val guardada = emprendedora.copy(
                    id = respDto.emprendedoraId.toInt(),
                    usuarioId = respDto.usuario.id.toInt(),
                    estadoFicha = estadoConfirmado,
                    activa = false
                )
                emprendedoraRepository.guardar(guardada)
                sesionDao.guardarSesion(
                    SesionActivaEntity(
                        id = 1,
                        rol = Rol.EMPRENDEDORA,
                        usuarioId = guardada.usuarioId,
                        correo = guardada.email,
                        emprendedoraId = guardada.id
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
                        correo = guardado.datos.email,
                        emprendedoraId = guardado.datos.id
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
                var tokenObtenido: String? = null
                try {
                    val loginResp = usuariosApi.login(LoginRequestDto(correoLimpio, clave))
                    if (loginResp.isSuccessful && loginResp.body() != null) {
                        tokenObtenido = loginResp.body()!!.token
                        ClienteRetrofit.tokenActivo = tokenObtenido
                    }
                } catch (_: Exception) {}

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
                        correo = cliente.email,
                        token = tokenObtenido
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
        val respuesta = try {
            usuariosApi.login(LoginRequestDto("camila.morales@textilcircular.cl", "emprendedora123"))
        } catch (_: Exception) { null }

        val tokenDemo = if (respuesta?.isSuccessful == true) respuesta.body()?.token else null
        val serverUsuarioId = if (respuesta?.isSuccessful == true) respuesta.body()?.usuario?.id?.toInt() else null
        if (tokenDemo != null) {
            ClienteRetrofit.tokenActivo = tokenDemo
        }

        val usuarioId = serverUsuarioId ?: 2
        val local = emprendedoraDao.obtenerPorIdDirecto(1)?.aModelo() ?: DatosDemo.EmprendedoraDemo
        val demo = local.copy(usuarioId = usuarioId)
        try {
            emprendedoraDao.insertar(EmprendedoraEntity.desdeModelo(demo))
        } catch (_: Exception) {}

        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.EMPRENDEDORA,
                usuarioId = usuarioId,
                correo = demo.email,
                token = tokenDemo,
                emprendedoraId = demo.id
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoEmprendedora(demo)
        Resultado.Exito(demo)
    }

    override suspend fun iniciarSesionDemoCliente(): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val respuesta = try {
            usuariosApi.login(LoginRequestDto("francisca.tapia@correo.cl", "cliente123"))
        } catch (_: Exception) { null }

        val tokenDemo = if (respuesta?.isSuccessful == true) respuesta.body()?.token else null
        val serverUsuarioId = if (respuesta?.isSuccessful == true) respuesta.body()?.usuario?.id?.toInt() else null
        if (tokenDemo != null) {
            ClienteRetrofit.tokenActivo = tokenDemo
        }

        val clienteId = serverUsuarioId ?: 14
        val demo = (clienteDao.obtenerPorIdDirecto(clienteId)?.aModelo() ?: DatosDemo.ClientesDemo.find { it.id == clienteId } ?: DatosDemo.ClienteDemo).copy(id = clienteId)
        try {
            clienteDao.insertar(ClienteEntity.desdeModelo(demo))
        } catch (_: Exception) {}

        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.CLIENTE,
                usuarioId = clienteId,
                correo = demo.email,
                token = tokenDemo
            )
        )
        _estadoSesion.value = EstadoSesion.AutenticadoCliente(demo)
        Resultado.Exito(demo)
    }

    override suspend fun iniciarSesionDemoAdmin(): Resultado<Unit> = withContext(Dispatchers.IO) {
        val respuesta = try {
            usuariosApi.login(LoginRequestDto("admin@textilcircular.cl", "admin123"))
        } catch (_: Exception) { null }

        val tokenDemo = if (respuesta?.isSuccessful == true) respuesta.body()?.token else null
        val serverUsuarioId = if (respuesta?.isSuccessful == true) respuesta.body()?.usuario?.id?.toInt() else null
        if (tokenDemo != null) {
            ClienteRetrofit.tokenActivo = tokenDemo
        }

        sesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.ADMINISTRADORA,
                usuarioId = serverUsuarioId ?: 1,
                correo = "admin@textilcircular.cl",
                token = tokenDemo
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
