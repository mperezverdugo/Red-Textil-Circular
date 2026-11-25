package com.example.textilcircular.remote

import com.example.textilcircular.data.local.dao.AdminDao
import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.ConteoAgrupado
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.CorreoAutomatizacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.UsuariosApi
import com.example.textilcircular.data.repository.AdminRepositoryRoom
import com.example.textilcircular.data.repository.ClienteRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryRoom
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.SesionRepositoryRoom
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.Rol
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetrofitUsuariosTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var usuariosApi: UsuariosApi

    private class FakeSesionDao : SesionDao {
        var sesion: SesionActivaEntity? = null
        override fun obtenerSesion(): Flow<SesionActivaEntity?> = flowOf(sesion)
        override suspend fun obtenerSesionDirecta(): SesionActivaEntity? = sesion
        override suspend fun guardarSesion(sesion: SesionActivaEntity) { this.sesion = sesion }
        override suspend fun cerrarSesion(): Int { sesion = null; return 1 }
    }

    private class FakeEmprendedoraDao : EmprendedoraDao {
        val lista = mutableListOf<EmprendedoraEntity>()
        override fun obtenerTodas(): Flow<List<EmprendedoraEntity>> = flowOf(lista)
        override fun obtenerPorId(id: Int): Flow<EmprendedoraEntity?> = flowOf(lista.find { it.id == id })
        override suspend fun obtenerPorIdDirecto(id: Int): EmprendedoraEntity? = lista.find { it.id == id }
        override suspend fun obtenerPorUsuarioIdDirecto(usuarioId: Int): EmprendedoraEntity? = lista.find { it.usuarioId == usuarioId }
        override suspend fun eliminarTodas(): Int {
            val count = lista.size
            lista.clear()
            return count
        }
        override suspend fun obtenerPorRutDirecto(rut: String): EmprendedoraEntity? = lista.find { it.rut == rut }
        override fun obtenerPorEmail(email: String): Flow<EmprendedoraEntity?> = flowOf(lista.find { it.email == email })
        override suspend fun obtenerPorEmailDirecto(email: String): EmprendedoraEntity? = lista.find { it.email == email }
        override fun obtenerMercado(busqueda: String, region: String?, comuna: String?): Flow<List<EmprendedoraEntity>> = flowOf(emptyList())
        override suspend fun insertar(emprendedora: EmprendedoraEntity): Long {
            lista.removeAll { it.id == emprendedora.id }
            lista.add(emprendedora)
            return emprendedora.id.toLong()
        }
        override suspend fun insertarVarias(emprendedoras: List<EmprendedoraEntity>) {
            emprendedoras.forEach { insertar(it) }
        }
        override suspend fun actualizar(emprendedora: EmprendedoraEntity): Int = 1
        override suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Int = 1
        override suspend fun cambiarEstadoFicha(id: Int, estado: EstadoFichaEmprendimiento, activa: Boolean): Int {
            val idx = lista.indexOfFirst { it.id == id }
            if (idx != -1) {
                lista[idx] = lista[idx].copy(estadoFicha = estado, activa = activa)
                return 1
            }
            return 0
        }
        override suspend fun eliminarPorId(id: Int): Int {
            val count = lista.count { it.id == id }
            lista.removeAll { it.id == id }
            return count
        }
    }

    private class FakeAdminDao(val emprendedoraDao: FakeEmprendedoraDao) : AdminDao {
        override fun obtenerFichas(): Flow<List<EmprendedoraEntity>> = flowOf(emprendedoraDao.lista)
        override fun obtenerFichaPorId(id: Int): Flow<EmprendedoraEntity?> = flowOf(emprendedoraDao.lista.find { it.id == id })
        override suspend fun registrarEvento(evento: MetricaEventoEntity): Long = 1L
        override fun contarEventos(tipo: String): Flow<Int> = flowOf(0)
        override fun contarFichasPorRevisar(): Flow<Int> = flowOf(emprendedoraDao.lista.count { it.estadoFicha == EstadoFichaEmprendimiento.POR_REVISAR })
        override fun contarContactosPorRegion(): Flow<List<ConteoAgrupado>> = flowOf(emptyList())
        override fun contarContactosPorRubro(): Flow<List<ConteoAgrupado>> = flowOf(emptyList())
        override fun obtenerCorreos(): Flow<List<CorreoAutomatizacionEntity>> = flowOf(emptyList())
        override suspend fun insertarCorreo(correo: CorreoAutomatizacionEntity): Long = 1L
        override suspend fun insertarCorreos(correos: List<CorreoAutomatizacionEntity>) {}
        override suspend fun actualizarEstadoCorreo(id: Int, estado: EstadoCorreo, fechaEnvio: String): Int = 1
    }

    private class FakeClienteDao : ClienteDao {
        val lista = mutableListOf<ClienteEntity>()
        override fun obtenerTodos(): Flow<List<ClienteEntity>> = flowOf(lista)
        override fun obtenerPorId(id: Int): Flow<ClienteEntity?> = flowOf(lista.find { it.id == id })
        override suspend fun obtenerPorIdDirecto(id: Int): ClienteEntity? = lista.find { it.id == id }
        override fun obtenerPorEmail(email: String): Flow<ClienteEntity?> = flowOf(lista.find { it.email == email })
        override suspend fun obtenerPorEmailDirecto(email: String): ClienteEntity? = lista.find { it.email == email }
        override suspend fun insertar(cliente: ClienteEntity): Long {
            lista.add(cliente)
            return cliente.id.toLong()
        }
        override suspend fun insertarVarios(clientes: List<ClienteEntity>) { lista.addAll(clientes) }
        override suspend fun actualizar(cliente: ClienteEntity): Int = 1
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val url = mockWebServer.url("/").toString()
        val client = OkHttpClient.Builder().build()
        usuariosApi = ClienteRetrofit.crearUsuariosApi(url, client)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        ClienteRetrofit.tokenActivo = null
    }

    private fun crearSesionRepo(): SesionRepositoryRoom {
        return SesionRepositoryRoom(
            sesionDao = FakeSesionDao(),
            emprendedoraDao = FakeEmprendedoraDao(),
            clienteDao = FakeClienteDao(),
            emprendedoraRepository = EmprendedoraRepositoryMemoria(),
            clienteRepository = ClienteRepositoryMemoria(),
            usuariosApi = usuariosApi
        )
    }

    @Test
    fun loginClienteExitoso() = runBlocking {
        val respuestaJson = """
            {
              "token": "token-jwt-cliente-prueba",
              "usuario": {
                "id": 14,
                "nombre": "Francisca Tapia",
                "email": "francisca.tapia@correo.cl",
                "rol": "CLIENTE"
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaJson))

        val repo = crearSesionRepo()
        val resultado = repo.iniciarSesionCliente("francisca.tapia@correo.cl", "clave123")

        assertTrue(resultado is Resultado.Exito)
        val cliente = (resultado as Resultado.Exito).datos
        assertEquals(14, cliente.id)
        assertEquals("francisca.tapia@correo.cl", cliente.email)
        assertEquals("token-jwt-cliente-prueba", ClienteRetrofit.tokenActivo)

        val peticion = mockWebServer.takeRequest()
        assertEquals("/auth/login", peticion.path)
        assertTrue(peticion.body.readUtf8().contains("francisca.tapia@correo.cl"))
    }

    @Test
    fun loginCredencialesInvalidasDevuelveError() = runBlocking {
        val errorJson = """
            {
              "codigo": "CREDENCIALES_INVALIDAS",
              "mensaje": "Correo o contraseña incorrectos"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(errorJson))

        val repo = crearSesionRepo()
        val resultado = repo.iniciarSesionCliente("ana@correo.cl", "clave-erronea")

        assertTrue(resultado is Resultado.Error)
        assertEquals("Correo o contraseña incorrectos", (resultado as Resultado.Error).mensaje)
    }

    @Test
    fun loginCuentaPorRevisarDevuelveError() = runBlocking {
        val errorJson = """
            {
              "codigo": "CUENTA_POR_REVISAR",
              "mensaje": "Tu registro está pendiente de revisión"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(403).setBody(errorJson))

        val repo = crearSesionRepo()
        val resultado = repo.iniciarSesionEmprendedora("patricia@correo.cl", "clave123")

        assertTrue(resultado is Resultado.Error)
        assertEquals("Tu registro está pendiente de revisión", (resultado as Resultado.Error).mensaje)
    }

    @Test
    fun registroClienteExitoso() = runBlocking {
        val respuestaJson = """
            {
              "id": 16,
              "nombre": "Nuevo Cliente",
              "email": "nuevo@correo.cl",
              "rol": "CLIENTE"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(respuestaJson))

        val repo = crearSesionRepo()
        val resultado = repo.registrarCliente("Nuevo Cliente", "nuevo@correo.cl", "clave123", true)

        assertTrue(resultado is Resultado.Exito)
        val cliente = (resultado as Resultado.Exito).datos
        assertEquals(16, cliente.id)
        assertEquals("nuevo@correo.cl", cliente.email)

        val peticion = mockWebServer.takeRequest()
        assertEquals("/auth/registro/cliente", peticion.path)
    }

    @Test
    fun registroEmprendedoraExitosa() = runBlocking {
        val respuestaJson = """
            {
              "usuario": {
                "id": 17,
                "nombre": "Artesana Nueva",
                "email": "artesana@correo.cl",
                "rol": "EMPRENDEDORA"
              },
              "emprendedoraId": 16,
              "estadoFicha": "POR_REVISAR"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(respuestaJson))

        val repo = crearSesionRepo()
        val nueva = Emprendedora(
            id = 0,
            rut = "15.420.312-5",
            nombre = "Artesana Nueva",
            nombreComercial = "Taller Nuevo",
            region = "Valparaíso",
            comuna = "Valparaíso",
            rubros = listOf(Rubro.REPARACION),
            email = "artesana@correo.cl"
        )

        val resultado = repo.registrarEmprendedora(nueva, "clave123")

        assertTrue(resultado is Resultado.Exito)
        val guardada = (resultado as Resultado.Exito).datos
        assertEquals(16, guardada.id)
        assertEquals(17, guardada.usuarioId)

        val peticion = mockWebServer.takeRequest()
        assertEquals("/auth/registro/emprendedora", peticion.path)
    }

    @Test
    fun listarEmprendedorasDesdeMicroservicio() = runBlocking {
        val respuestaJson = """
            [
              {
                "id": 1,
                "usuarioId": 2,
                "nombreComercial": "Taller Hilos del Sur",
                "region": "Valparaíso",
                "comuna": "Valparaíso",
                "rubros": ["SUPRARRECICLAJE", "REPARACION"],
                "instagram": "tallerhilosdelsur"
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaJson))

        val repo = EmprendedoraRepositoryRoom(
            emprendedoraDao = FakeEmprendedoraDao(),
            usuariosApi = usuariosApi
        )

        val lista = repo.obtenerMercado("Hilos", "Valparaíso", "Valparaíso", Rubro.REPARACION).first()

        assertEquals(1, lista.size)
        val emp = lista.first()
        assertEquals(1, emp.id)
        assertEquals(2, emp.usuarioId)
        assertEquals("Taller Hilos del Sur", emp.nombreComercial)
        assertEquals(2, emp.rubros.size)
        assertTrue(emp.rubros.contains(Rubro.SUPRARRECICLAJE))

        val peticion = mockWebServer.takeRequest()
        assertTrue(peticion.path!!.startsWith("/emprendedoras?"))
        assertTrue(peticion.path!!.contains("texto=Hilos"))
        assertTrue(peticion.path!!.contains("rubro=REPARACION"))
    }

    @Test
    fun sincronizarFichasAdminDescargaPendientes() = runBlocking {
        ClienteRetrofit.tokenActivo = "token-admin-jwt"
        val respuestaJson = """
            [
              {
                "id": 10,
                "usuarioId": 11,
                "nombre": "Elena Gómez",
                "email": "elena@correo.cl",
                "rut": "14.567.890-2",
                "nombreComercial": "Taller EcoTextil",
                "region": "Metropolitana de Santiago",
                "comuna": "Providencia",
                "rubros": ["SUPRARRECICLAJE"],
                "instagram": "ecotextil",
                "activa": true,
                "estadoFicha": "POR_REVISAR",
                "motivoRevision": null,
                "fechaRevision": null
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaJson))

        val dao = FakeEmprendedoraDao()
        val adminDao = FakeAdminDao(dao)
        val adminRepo = AdminRepositoryRoom(
            adminDao = adminDao,
            emprendedoraDao = dao,
            usuariosApi = usuariosApi
        )

        val resultado = adminRepo.sincronizarFichas()
        assertTrue(resultado is Resultado.Exito)

        assertEquals(1, dao.lista.size)
        val entidad = dao.lista.first()
        assertEquals(10, entidad.id)
        assertEquals(EstadoFichaEmprendimiento.POR_REVISAR, entidad.estadoFicha)
        assertEquals(false, entidad.activa) // Por revisar no debe ser visible públicamente

        val peticion = mockWebServer.takeRequest()
        assertEquals("/admin/emprendedoras", peticion.path)
        assertEquals("Bearer token-admin-jwt", peticion.getHeader("Authorization"))
    }

    @Test
    fun moderarFichaAprobarActualizaEstadoYActiva() = runBlocking {
        ClienteRetrofit.tokenActivo = "token-admin-jwt"
        val respuestaPatchJson = """
            {
              "id": 10,
              "estadoFicha": "ACTIVA",
              "motivoRevision": null,
              "fechaRevision": "2026-11-25T10:00:00Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaPatchJson))

        // Segunda llamada para re-sincronizar tras moderar
        val respuestaListarJson = """
            [
              {
                "id": 10,
                "usuarioId": 11,
                "nombre": "Elena Gómez",
                "email": "elena@correo.cl",
                "rut": "14.567.890-2",
                "nombreComercial": "Taller EcoTextil",
                "region": "Metropolitana de Santiago",
                "comuna": "Providencia",
                "rubros": ["SUPRARRECICLAJE"],
                "instagram": "ecotextil",
                "activa": true,
                "estadoFicha": "ACTIVA",
                "motivoRevision": null,
                "fechaRevision": "2026-11-25T10:00:00Z"
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaListarJson))

        val dao = FakeEmprendedoraDao()
        val adminDao = FakeAdminDao(dao)
        val adminRepo = AdminRepositoryRoom(
            adminDao = adminDao,
            emprendedoraDao = dao,
            usuariosApi = usuariosApi
        )

        val resultado = adminRepo.cambiarEstadoFicha(10, EstadoFichaEmprendimiento.ACTIVA)
        assertTrue(resultado is Resultado.Exito)

        val reqPatch = mockWebServer.takeRequest()
        assertEquals("/admin/emprendedoras/10/revision", reqPatch.path)
        assertEquals("PATCH", reqPatch.method)
        assertTrue(reqPatch.body.readUtf8().contains("APROBAR"))

        val reqSync = mockWebServer.takeRequest()
        assertEquals("/admin/emprendedoras", reqSync.path)

        assertEquals(1, dao.lista.size)
        val fichaActualizada = dao.lista.first()
        assertEquals(EstadoFichaEmprendimiento.ACTIVA, fichaActualizada.estadoFicha)
        assertEquals(true, fichaActualizada.activa)
    }

    @Test
    fun loginDemoAdminAutenticaYPersisteToken() = runBlocking {
        val respuestaLoginJson = """
            {
              "token": "token-jwt-admin-demo",
              "usuario": {
                "id": 1,
                "nombre": "Administradora",
                "email": "admin@textilcircular.cl",
                "rol": "ADMINISTRADORA"
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaLoginJson))

        val sesionDao = FakeSesionDao()
        val repo = SesionRepositoryRoom(
            sesionDao = sesionDao,
            emprendedoraDao = FakeEmprendedoraDao(),
            clienteDao = FakeClienteDao(),
            emprendedoraRepository = EmprendedoraRepositoryMemoria(),
            clienteRepository = ClienteRepositoryMemoria(),
            usuariosApi = usuariosApi
        )

        val resultado = repo.iniciarSesionDemoAdmin()
        assertTrue(resultado is Resultado.Exito)

        val sesionActual = repo.estadoSesion.value
        assertEquals(EstadoSesion.AutenticadoAdmin, sesionActual)
        assertEquals(Rol.ADMINISTRADORA, sesionActual.rol)
        assertEquals("token-jwt-admin-demo", ClienteRetrofit.tokenActivo)

        val peticion = mockWebServer.takeRequest()
        assertEquals("/auth/login", peticion.path)
        assertTrue(peticion.body.readUtf8().contains("admin@textilcircular.cl"))
    }

    @Test
    fun loginDemoClienteAutenticaYPersisteTokenConId14() = runBlocking {
        val respuestaLoginJson = """
            {
              "token": "token-jwt-cliente-demo",
              "usuario": {
                "id": 14,
                "nombre": "Francisca Tapia",
                "email": "francisca.tapia@correo.cl",
                "rol": "CLIENTE"
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaLoginJson))

        val sesionDao = FakeSesionDao()
        val clienteDao = FakeClienteDao()
        val repo = SesionRepositoryRoom(
            sesionDao = sesionDao,
            emprendedoraDao = FakeEmprendedoraDao(),
            clienteDao = clienteDao,
            emprendedoraRepository = EmprendedoraRepositoryMemoria(),
            clienteRepository = ClienteRepositoryMemoria(),
            usuariosApi = usuariosApi
        )

        val resultado = repo.iniciarSesionDemoCliente()
        assertTrue(resultado is Resultado.Exito)

        val sesionActual = repo.estadoSesion.value
        assertTrue(sesionActual is EstadoSesion.AutenticadoCliente)
        val cliente = (sesionActual as EstadoSesion.AutenticadoCliente).cliente
        assertEquals(14, cliente.id)
        assertEquals("francisca.tapia@correo.cl", cliente.email)
        assertEquals("token-jwt-cliente-demo", ClienteRetrofit.tokenActivo)

        // Verificamos que se guardó en SesionDao con token e id 14
        val sesionGuardada = sesionDao.obtenerSesionDirecta()
        assertEquals("token-jwt-cliente-demo", sesionGuardada?.token)
        assertEquals(14, sesionGuardada?.usuarioId)
    }

    @Test
    fun registroClienteAutoLoginObtieneTokenValido() = runBlocking {
        val respuestaRegistroJson = """
            {
              "id": 18,
              "nombre": "Cliente Nuevo",
              "email": "clientenuevo@correo.cl",
              "rol": "CLIENTE"
            }
        """.trimIndent()
        val respuestaLoginJson = """
            {
              "token": "token-autologin-cliente",
              "usuario": {
                "id": 18,
                "nombre": "Cliente Nuevo",
                "email": "clientenuevo@correo.cl",
                "rol": "CLIENTE"
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(respuestaRegistroJson))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaLoginJson))

        val repo = crearSesionRepo()
        val res = repo.registrarCliente("Cliente Nuevo", "clientenuevo@correo.cl", "clave123", true)

        assertTrue(res is Resultado.Exito)
        assertEquals("token-autologin-cliente", ClienteRetrofit.tokenActivo)

        val req1 = mockWebServer.takeRequest()
        assertEquals("/auth/registro/cliente", req1.path)
        val req2 = mockWebServer.takeRequest()
        assertEquals("/auth/login", req2.path)
    }

    @Test
    fun loginUnificadoEmprendedoraSincronizaFichaAprobada() = runBlocking {
        val respuestaLoginJson = """
            {
              "token": "token-jwt-emprendedora-aprobada",
              "usuario": {
                "id": 20,
                "nombre": "Camila Aprobada",
                "email": "camila.aprobada@correo.cl",
                "rol": "EMPRENDEDORA"
              }
            }
        """.trimIndent()
        val respuestaPerfilMeJson = """
            {
              "id": 20,
              "nombre": "Camila Aprobada",
              "email": "camila.aprobada@correo.cl",
              "rol": "EMPRENDEDORA",
              "aceptaLeyDatos": true,
              "ficha": {
                "id": 15,
                "rut": "15.420.312-5",
                "nombreComercial": "Taller Aprobado",
                "region": "Valparaíso",
                "comuna": "Valparaíso",
                "rubros": ["REPARACION"],
                "instagram": "taller_aprobado",
                "activa": true,
                "estadoFicha": "ACTIVA",
                "motivoRevision": null,
                "fechaRevision": "2026-11-25T11:00:00Z"
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaLoginJson))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaPerfilMeJson))

        val repo = crearSesionRepo()
        val res = repo.iniciarSesion("camila.aprobada@correo.cl", "clave123")

        assertTrue(res is Resultado.Exito)
        assertEquals(Rol.EMPRENDEDORA, (res as Resultado.Exito).datos)

        val estado = repo.estadoSesion.value
        assertTrue(estado is EstadoSesion.AutenticadoEmprendedora)
        val emp = (estado as EstadoSesion.AutenticadoEmprendedora).emprendedora
        assertEquals(15, emp.id)
        assertEquals(20, emp.usuarioId)
        assertEquals(EstadoFichaEmprendimiento.ACTIVA, emp.estadoFicha)
        assertEquals(true, emp.activa)
        assertEquals("Taller Aprobado", emp.nombreComercial)
    }

    @Test
    fun loginClienteRechazaUsuarioConRolEmprendedora() = runBlocking {
        val respuestaLoginJson = """
            {
              "token": "token-jwt-emprendedora",
              "usuario": {
                "id": 25,
                "nombre": "Emprendedora No Cliente",
                "email": "emprendedora@correo.cl",
                "rol": "EMPRENDEDORA"
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaLoginJson))

        val repo = crearSesionRepo()
        val res = repo.iniciarSesionCliente("emprendedora@correo.cl", "clave123")

        assertTrue(res is Resultado.Error)
        assertTrue((res as Resultado.Error).mensaje.contains("no tiene rol de cliente"))
    }
}
