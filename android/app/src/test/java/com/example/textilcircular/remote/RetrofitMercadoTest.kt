package com.example.textilcircular.remote

import com.example.textilcircular.data.local.dao.MensajeDao
import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.MercadoApi
import com.example.textilcircular.data.repository.MensajeRepositoryRoom
import com.example.textilcircular.data.repository.ProductoRepositoryRoom
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.TipoEmisor
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

class RetrofitMercadoTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var mercadoApi: MercadoApi

    private class FakeProductoDao : ProductoDao {
        val productos = mutableListOf<ProductoEntity>()
        override fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<ProductoEntity>> =
            flowOf(productos.filter { it.emprendedoraId == emprendedoraId })
        override fun obtenerPorId(id: Int): Flow<ProductoEntity?> =
            flowOf(productos.find { it.id == id })
        override suspend fun obtenerPorIdDirecto(id: Int): ProductoEntity? =
            productos.find { it.id == id }
        override fun obtenerTodos(): Flow<List<ProductoEntity>> = flowOf(productos)
        override suspend fun insertar(producto: ProductoEntity): Long {
            productos.add(producto)
            return producto.id.toLong()
        }
        override suspend fun insertarVarios(productos: List<ProductoEntity>) { this.productos.addAll(productos) }
        override suspend fun actualizar(producto: ProductoEntity): Int = 1
        override suspend fun eliminarPorId(id: Int): Int {
            productos.removeAll { it.id == id }
            return 1
        }
        override suspend fun marcarTodosSincronizados(): Int = 1
        override suspend fun limpiarPendienteProducto(id: Int): Int = 1
    }

    private class FakeMensajeDao : MensajeDao {
        val conversaciones = mutableListOf<ConversacionEntity>()
        val mensajes = mutableListOf<MensajeEntity>()

        override fun obtenerConversacionesEmprendedora(emprendedoraId: Int): Flow<List<ConversacionEntity>> =
            flowOf(conversaciones.filter { it.emprendedoraId == emprendedoraId })
        override fun obtenerConversacionesCliente(clienteId: Int): Flow<List<ConversacionEntity>> =
            flowOf(conversaciones.filter { it.clienteId == clienteId })
        override fun obtenerConversacionPorId(id: Int): Flow<ConversacionEntity?> =
            flowOf(conversaciones.find { it.id == id })
        override suspend fun obtenerConversacionPorIdDirecto(id: Int): ConversacionEntity? =
            conversaciones.find { it.id == id }
        override fun obtenerConversacionEntre(clienteId: Int, emprendedoraId: Int): Flow<ConversacionEntity?> =
            flowOf(conversaciones.find { it.clienteId == clienteId && it.emprendedoraId == emprendedoraId })
        override suspend fun obtenerConversacionEntreDirecto(clienteId: Int, emprendedoraId: Int): ConversacionEntity? =
            conversaciones.find { it.clienteId == clienteId && it.emprendedoraId == emprendedoraId }
        override suspend fun insertarConversacion(conversacion: ConversacionEntity): Long {
            conversaciones.add(conversacion)
            return conversacion.id.toLong()
        }
        override suspend fun insertarConversaciones(conversaciones: List<ConversacionEntity>) {
            this.conversaciones.addAll(conversaciones)
        }
        override suspend fun actualizarConversacion(conversacion: ConversacionEntity): Int = 1
        override fun obtenerMensajesPorConversacion(conversacionId: Int): Flow<List<MensajeEntity>> =
            flowOf(mensajes.filter { it.conversacionId == conversacionId })
        override suspend fun insertarMensaje(mensaje: MensajeEntity): Long {
            mensajes.add(mensaje)
            return mensaje.id.toLong()
        }
        override suspend fun insertarMensajes(mensajes: List<MensajeEntity>) { this.mensajes.addAll(mensajes) }
        override suspend fun limpiarNoLeidosEmprendedora(conversacionId: Int): Int = 1
        override suspend fun limpiarNoLeidosCliente(conversacionId: Int): Int = 1
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val url = mockWebServer.url("/").toString()
        val client = OkHttpClient.Builder().build()
        mercadoApi = ClienteRetrofit.crearMercadoApi(url, client)
        ClienteRetrofit.tokenActivo = "token-jwt-prueba"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        ClienteRetrofit.tokenActivo = null
    }

    @Test
    fun listarProductosPorEmprendedoraDesdeMicroservicio() = runBlocking {
        val json = """
            [
              {
                "id": 1,
                "emprendedoraId": 2,
                "nombre": "Bolso Upcycling Mezclilla",
                "descripcionCorta": "Bolso reutilizado de retazos de mezclilla resistente.",
                "explicacionProceso": "Confeccionado artesanalmente rescatando jeans en desuso."
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val repo = ProductoRepositoryRoom(
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val lista = repo.obtenerPorEmprendedora(2).first()

        assertEquals(1, lista.size)
        val prod = lista.first()
        assertEquals(1, prod.id)
        assertEquals(2, prod.emprendedoraId)
        assertEquals("Bolso Upcycling Mezclilla", prod.nombre)

        val request = mockWebServer.takeRequest()
        assertEquals("/productos?emprendedoraId=2", request.path)
    }

    @Test
    fun crearProductoExitoso() = runBlocking {
        val respuestaJson = """
            {
              "id": 10,
              "emprendedoraId": 2,
              "nombre": "Cojín Textil Reciclado",
              "descripcionCorta": "Cojín decorativo con relleno sustentable.",
              "explicacionProceso": "Relleno con sobrantes textiles limpios."
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(respuestaJson))

        val repo = ProductoRepositoryRoom(
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val nuevo = Producto(
            id = 0,
            emprendedoraId = 2,
            nombre = "Cojín Textil Reciclado",
            descripcionCorta = "Cojín decorativo con relleno sustentable.",
            explicacionProceso = "Relleno con sobrantes textiles limpios."
        )

        val resultado = repo.guardar(nuevo)

        assertTrue(resultado is Resultado.Exito)
        val guardado = (resultado as Resultado.Exito).datos
        assertEquals(10, guardado.id)
        assertEquals(2, guardado.emprendedoraId)

        val request = mockWebServer.takeRequest()
        assertEquals("/productos", request.path)
        assertEquals("POST", request.method)
        assertEquals("Bearer token-jwt-prueba", request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("Cojín Textil Reciclado"))
    }

    @Test
    fun editarProductoExitoso() = runBlocking {
        val respuestaJson = """
            {
              "id": 10,
              "emprendedoraId": 2,
              "nombre": "Cojín Textil Modificado",
              "descripcionCorta": "Cojín decorativo modificado con retazos.",
              "explicacionProceso": "Actualización de confección."
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaJson))

        val repo = ProductoRepositoryRoom(
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val producto = Producto(
            id = 10,
            emprendedoraId = 2,
            nombre = "Cojín Textil Modificado",
            descripcionCorta = "Cojín decorativo modificado con retazos.",
            explicacionProceso = "Actualización de confección."
        )

        val resultado = repo.actualizar(producto)

        assertTrue(resultado is Resultado.Exito)

        val request = mockWebServer.takeRequest()
        assertEquals("/productos/10", request.path)
        assertEquals("PUT", request.method)
        assertEquals("Bearer token-jwt-prueba", request.getHeader("Authorization"))
    }

    @Test
    fun eliminarProductoExitoso() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        val repo = ProductoRepositoryRoom(
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val resultado = repo.eliminar(10)

        assertTrue(resultado is Resultado.Exito)

        val request = mockWebServer.takeRequest()
        assertEquals("/productos/10", request.path)
        assertEquals("DELETE", request.method)
        assertEquals("Bearer token-jwt-prueba", request.getHeader("Authorization"))
    }

    @Test
    fun listarConversacionesDesdeMicroservicio() = runBlocking {
        val json = """
            [
              {
                "id": 1,
                "clienteId": 14,
                "emprendedoraId": 2,
                "productoId": 1,
                "nombreCliente": "Francisca Tapia",
                "nombreEmprendimiento": "Taller Hilos del Sur",
                "ultimoMensaje": "Hola, ¿sigue disponible?",
                "fechaHora": "2026-11-22T15:30:00Z",
                "noLeidosCliente": 0,
                "noLeidosEmprendedora": 1
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val repo = MensajeRepositoryRoom(
            mensajeDao = FakeMensajeDao(),
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val conversaciones = repo.obtenerConversaciones(2).first()

        assertEquals(1, conversaciones.size)
        val conv = conversaciones.first()
        assertEquals(1, conv.id)
        assertEquals(14, conv.clienteId)
        assertEquals(2, conv.emprendedoraId)
        assertEquals("Francisca Tapia", conv.nombreCliente)
        assertEquals(1, conv.noLeidosEmprendedora)

        val request = mockWebServer.takeRequest()
        assertEquals("/conversaciones", request.path)
        assertEquals("GET", request.method)
        assertEquals("Bearer token-jwt-prueba", request.getHeader("Authorization"))
    }

    @Test
    fun iniciarOObtenerConversacionYEnviarMensaje() = runBlocking {
        val convJson = """
            {
              "id": 1,
              "clienteId": 14,
              "emprendedoraId": 2,
              "productoId": 1,
              "nombreCliente": "Francisca Tapia",
              "nombreEmprendimiento": "Taller Hilos del Sur",
              "ultimoMensaje": "Hola, me interesa este bolso",
              "fechaHora": "2026-11-22T15:30:00Z",
              "noLeidosCliente": 0,
              "noLeidosEmprendedora": 1
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(convJson))

        val msgJson = """
            {
              "id": 1,
              "conversacionId": 1,
              "emisor": "CLIENTE",
              "contenido": "Hola, me interesa este bolso",
              "fechaHora": "2026-11-22T15:30:00Z",
              "esLeido": false,
              "productoId": 1,
              "nombreProducto": "Bolso Upcycling Mezclilla"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(msgJson))

        val repo = MensajeRepositoryRoom(
            mensajeDao = FakeMensajeDao(),
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val prod = Producto(id = 1, emprendedoraId = 2, nombre = "Bolso Upcycling Mezclilla", descripcionCorta = "")
        val resultado = repo.iniciarOObtenerConversacion(
            clienteId = 14,
            emprendedoraId = 2,
            nombreCliente = "Francisca Tapia",
            nombreEmprendimiento = "Taller Hilos del Sur",
            primerMensaje = "Hola, me interesa este bolso",
            emisor = TipoEmisor.CLIENTE,
            productoAsociado = prod
        )

        assertTrue(resultado is Resultado.Exito)
        val conv = (resultado as Resultado.Exito).datos
        assertEquals(1, conv.id)
        assertEquals(14, conv.clienteId)

        val req1 = mockWebServer.takeRequest()
        assertEquals("/conversaciones", req1.path)
        assertEquals("POST", req1.method)
        assertTrue(req1.body.readUtf8().contains("Francisca Tapia"))

        val req2 = mockWebServer.takeRequest()
        assertEquals("/conversaciones/1/mensajes", req2.path)
        assertEquals("POST", req2.method)
        assertTrue(req2.body.readUtf8().contains("Hola, me interesa este bolso"))
    }

    @Test
    fun listarMensajesDesdeMicroservicio() = runBlocking {
        val json = """
            [
              {
                "id": 1,
                "conversacionId": 1,
                "emisor": "CLIENTE",
                "contenido": "Hola, ¿sigue disponible?",
                "fechaHora": "2026-11-22T15:30:00Z",
                "esLeido": true,
                "productoId": null,
                "nombreProducto": null
              },
              {
                "id": 2,
                "conversacionId": 1,
                "emisor": "EMPRENDEDORA",
                "contenido": "¡Hola! Sí, todavía lo tengo.",
                "fechaHora": "2026-11-22T15:32:00Z",
                "esLeido": false,
                "productoId": null,
                "nombreProducto": null
              }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val repo = MensajeRepositoryRoom(
            mensajeDao = FakeMensajeDao(),
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val mensajes = repo.obtenerMensajes(1).first()

        assertEquals(2, mensajes.size)
        assertEquals(TipoEmisor.CLIENTE, mensajes[0].emisor)
        assertEquals("Hola, ¿sigue disponible?", mensajes[0].contenido)
        assertEquals(TipoEmisor.EMPRENDEDORA, mensajes[1].emisor)
        assertEquals("¡Hola! Sí, todavía lo tengo.", mensajes[1].contenido)

        val req = mockWebServer.takeRequest()
        assertEquals("/conversaciones/1/mensajes", req.path)
    }

    @Test
    fun marcarLecturaLlamaEndpoint() = runBlocking {
        val respuestaJson = """
            {
              "id": 1,
              "clienteId": 14,
              "emprendedoraId": 2,
              "productoId": null,
              "nombreCliente": "Francisca Tapia",
              "nombreEmprendimiento": "Taller Hilos del Sur",
              "ultimoMensaje": "¡Hola!",
              "fechaHora": "2026-11-22T15:30:00Z",
              "noLeidosCliente": 0,
              "noLeidosEmprendedora": 0
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(respuestaJson))

        val repo = MensajeRepositoryRoom(
            mensajeDao = FakeMensajeDao(),
            productoDao = FakeProductoDao(),
            mercadoApi = mercadoApi
        )

        val resultado = repo.marcarComoLeidoEmprendedora(1)

        assertTrue(resultado is Resultado.Exito)

        val req = mockWebServer.takeRequest()
        assertEquals("/conversaciones/1/lectura", req.path)
        assertEquals("PATCH", req.method)
        assertEquals("Bearer token-jwt-prueba", req.getHeader("Authorization"))
    }
}
