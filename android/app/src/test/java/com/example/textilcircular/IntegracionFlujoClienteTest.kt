package com.example.textilcircular

import com.example.textilcircular.data.repository.AdminRepositoryMemoria
import com.example.textilcircular.data.repository.ClienteRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.MensajeRepositoryMemoria
import com.example.textilcircular.data.repository.ProductoRepositoryMemoria
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.data.repository.SincronizacionRepositoryMemoria
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.Rol
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.screens.cliente.MiPerfilClienteViewModel
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Pruebas de integración de extremo a extremo y verificación de no regresión en memoria.
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionFlujoClienteTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var empRepo: EmprendedoraRepositoryMemoria
    private lateinit var clienteRepo: ClienteRepositoryMemoria
    private lateinit var sesionRepo: SesionRepositoryMemoria
    private lateinit var productoRepo: ProductoRepositoryMemoria
    private lateinit var mensajeRepo: MensajeRepositoryMemoria
    private lateinit var syncRepo: SincronizacionRepositoryMemoria
    private lateinit var adminRepo: AdminRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        empRepo = EmprendedoraRepositoryMemoria()
        clienteRepo = ClienteRepositoryMemoria()
        sesionRepo = SesionRepositoryMemoria(empRepo, clienteRepo)
        productoRepo = ProductoRepositoryMemoria()
        mensajeRepo = MensajeRepositoryMemoria()
        syncRepo = SincronizacionRepositoryMemoria()
        adminRepo = AdminRepositoryMemoria(empRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun flujoCompleto_registroClienteExploracionYContactoArtesana() = runTest {
        // 1. Registro del nuevo cliente
        val registroRes = sesionRepo.registrarCliente(
            nombre = "Martina Valenzuela",
            correo = "martina.valenzuela@correo.cl",
            clave = "Password123!",
            aceptaDatos = true
        )
        assertTrue(registroRes is Resultado.Exito)
        val nuevoCliente = (registroRes as Resultado.Exito).datos
        assertEquals("Martina Valenzuela", nuevoCliente.nombre)

        val sesionTrasRegistro = sesionRepo.estadoSesion.value
        assertTrue(sesionTrasRegistro is EstadoSesion.AutenticadoCliente)

        // 2. Búsqueda y filtrado en el mercado
        adminRepo.registrarBusqueda(termino = "telar", region = "Valparaíso", rubro = "Upcycling")

        // 3. Clic de contacto desde la ficha del taller
        adminRepo.registrarClicContacto(region = "Valparaíso", rubro = "Upcycling")

        // 4. Contacto y envío del primer mensaje
        val inicioConvRes = mensajeRepo.iniciarOObtenerConversacion(
            clienteId = nuevoCliente.id,
            emprendedoraId = 1,
            nombreCliente = nuevoCliente.nombre,
            nombreEmprendimiento = "Taller Hilos del Sur",
            primerMensaje = "",
            emisor = TipoEmisor.CLIENTE
        )
        assertTrue(inicioConvRes is Resultado.Exito)
        val convId = (inicioConvRes as Resultado.Exito).datos.id

        val envMsgRes = mensajeRepo.enviarMensaje(
            conversacionId = convId,
            contenido = "Hola, me interesa encargar un bolso personalizado",
            emisor = TipoEmisor.CLIENTE,
            productoId = 101,
            nombreProducto = "Bolso Upcycling Denim"
        )
        assertTrue(envMsgRes is Resultado.Exito)
        adminRepo.registrarConsultaEnviada(region = "Valparaíso", rubro = "Upcycling")

        // 5. La artesana ve la conversación con 1 mensaje no leído
        val convsArtesana = mensajeRepo.obtenerConversaciones(1).first()
        val convRecibida = convsArtesana.find { it.id == convId }
        assertNotNull(convRecibida)
        assertEquals(1, convRecibida?.noLeidosEmprendedora)

        // 6. La artesana abre la conversación y responde
        mensajeRepo.marcarComoLeidoEmprendedora(convId)
        val convLeida = mensajeRepo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convLeida?.noLeidosEmprendedora)

        mensajeRepo.enviarMensaje(
            conversacionId = convId,
            contenido = "¡Hola Martina! Con gusto coordinamos tu diseño.",
            emisor = TipoEmisor.EMPRENDEDORA
        )

        // 7. El cliente recibe la respuesta y la lee
        val convsCliente = mensajeRepo.obtenerConversacionesCliente(nuevoCliente.id).first()
        assertEquals(1, convsCliente.sumOf { it.noLeidosCliente })

        mensajeRepo.marcarComoLeidoCliente(convId)
        val convClienteLeida = mensajeRepo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convClienteLeida?.noLeidosCliente)

        // 8. El cliente edita su nombre en su perfil
        val perfilViewModel = MiPerfilClienteViewModel(sesionRepo, clienteRepo)
        advanceUntilIdle()

        perfilViewModel.abrirDialogoEdicion()
        perfilViewModel.onNuevoNombreCambiado("Martina V. Pérez")
        perfilViewModel.guardarNombre()
        advanceUntilIdle()

        val sesionConNuevoNombre = sesionRepo.estadoSesion.value
        assertEquals("Martina V. Pérez", (sesionConNuevoNombre as EstadoSesion.AutenticadoCliente).cliente.nombre)

        // 9. El cliente cierra sesión
        perfilViewModel.cerrarSesion {}
        advanceUntilIdle()
        assertTrue(sesionRepo.estadoSesion.value is EstadoSesion.NoAutenticado)
    }

    @Test
    fun metricasAdmin_seActualizanConAccionesDelCliente() = runTest {
        val metricasIniciales = adminRepo.obtenerMetricas().first()
        val busquedasPrevias = metricasIniciales.totalBusquedas
        val clicsPrevios = metricasIniciales.totalClics
        val consultasPrevias = metricasIniciales.totalConsultas

        adminRepo.registrarBusqueda(termino = "mezclilla", region = "Maule", rubro = "Confección y reparación")
        adminRepo.registrarClicContacto(region = "Maule", rubro = "Confección y reparación")
        adminRepo.registrarConsultaEnviada(region = "Maule", rubro = "Confección y reparación")

        val metricasNuevas = adminRepo.obtenerMetricas().first()
        assertEquals(busquedasPrevias + 1, metricasNuevas.totalBusquedas)
        assertEquals(clicsPrevios + 1, metricasNuevas.totalClics)
        assertEquals(consultasPrevias + 1, metricasNuevas.totalConsultas)
    }

    @Test
    fun aislamientoRoles_rutasYSesionesPorRol() = runTest {
        // Sesión cliente
        sesionRepo.iniciarSesionDemoCliente()
        val sesionCli = sesionRepo.estadoSesion.value
        assertEquals(Rol.CLIENTE, sesionCli.rol)

        // Sesión emprendedora
        sesionRepo.iniciarSesionDemoEmprendedora()
        val sesionEmp = sesionRepo.estadoSesion.value
        assertEquals(Rol.EMPRENDEDORA, sesionEmp.rol)

        // Sesión administradora
        sesionRepo.iniciarSesionDemoAdmin()
        val sesionAdm = sesionRepo.estadoSesion.value
        assertEquals(Rol.ADMINISTRADORA, sesionAdm.rol)

        // Cierre de sesión
        sesionRepo.cerrarSesion()
        val sesionCerrada = sesionRepo.estadoSesion.value
        assertEquals(null, sesionCerrada.rol)
    }

    @Test
    fun regresion_funcionalidadesEmprendedoraSiguenOperativas() = runTest {
        // Catálogo de la emprendedora
        val nuevoProducto = Producto(
            id = 999,
            emprendedoraId = 1,
            nombre = "Pieza Textil Recuperada",
            descripcionCorta = "Prenda única confeccionada con descartes.",
            fotos = listOf("foto_prueba")
        )
        val resProd = productoRepo.guardar(nuevoProducto)
        assertTrue(resProd is Resultado.Exito)

        val prodsEmp1 = productoRepo.obtenerPorEmprendedora(1).first()
        assertTrue(prodsEmp1.any { it.id == 999 })

        // Sincronización
        syncRepo.registrarPendiente(tipo = "Nuevo producto", detalle = "Pieza Textil Recuperada")
        val pendientes = syncRepo.obtenerPendientes().first()
        assertTrue(pendientes.isNotEmpty())

        val resSync = syncRepo.sincronizar()
        assertTrue(resSync is Resultado.Exito)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, syncRepo.estadoSincronizacion.first())
        assertEquals(0, syncRepo.obtenerPendientes().first().size)
    }
}
