package com.example.textilcircular.remote

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.UsuariosApi
import com.example.textilcircular.data.repository.ClienteRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryRoom
import com.example.textilcircular.data.repository.SesionRepositoryRoom
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
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
        override suspend fun obtenerPorRutDirecto(rut: String): EmprendedoraEntity? = lista.find { it.rut == rut }
        override fun obtenerPorEmail(email: String): Flow<EmprendedoraEntity?> = flowOf(lista.find { it.email == email })
        override suspend fun obtenerPorEmailDirecto(email: String): EmprendedoraEntity? = lista.find { it.email == email }
        override fun obtenerMercado(busqueda: String, region: String?, comuna: String?): Flow<List<EmprendedoraEntity>> = flowOf(emptyList())
        override suspend fun insertar(emprendedora: EmprendedoraEntity): Long {
            lista.add(emprendedora)
            return emprendedora.id.toLong()
        }
        override suspend fun insertarVarias(emprendedoras: List<EmprendedoraEntity>) { lista.addAll(emprendedoras) }
        override suspend fun actualizar(emprendedora: EmprendedoraEntity): Int = 1
        override suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Int = 1
        override suspend fun cambiarEstadoFicha(id: Int, estado: EstadoFichaEmprendimiento, activa: Boolean): Int = 1
        override suspend fun eliminarPorId(id: Int): Int = 1
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
}
