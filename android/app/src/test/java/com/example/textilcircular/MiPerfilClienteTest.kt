package com.example.textilcircular

import com.example.textilcircular.data.repository.ClienteRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.MensajeRepositoryMemoria
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.screens.cliente.MiPerfilClienteViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Pruebas unitarias para la consulta y edición de perfil del cliente, cierre de sesión y contador de no leídos.
@OptIn(ExperimentalCoroutinesApi::class)
class MiPerfilClienteTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var empRepo: EmprendedoraRepositoryMemoria
    private lateinit var clienteRepo: ClienteRepositoryMemoria
    private lateinit var sesionRepo: SesionRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        empRepo = EmprendedoraRepositoryMemoria()
        clienteRepo = ClienteRepositoryMemoria()
        sesionRepo = SesionRepositoryMemoria(empRepo, clienteRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun editarNombre_conNombreValido_actualizaEnRepositorioYSesion() = runTest {
        // Iniciamos sesión con el cliente de prueba
        sesionRepo.iniciarSesionDemoCliente()

        val viewModel = MiPerfilClienteViewModel(sesionRepo, clienteRepo)
        advanceUntilIdle()

        viewModel.abrirDialogoEdicion()
        viewModel.onNuevoNombreCambiado("Francisca Tapia Morales")
        viewModel.guardarNombre()
        advanceUntilIdle()

        // Validamos que el ViewModel, la sesión y el repositorio contengan el nuevo nombre
        val estadoVm = viewModel.uiState.value
        assertEquals("Francisca Tapia Morales", estadoVm.nombre)
        assertNull(estadoVm.errorNombre)

        val estadoSesion = sesionRepo.estadoSesion.value
        assertTrue(estadoSesion is EstadoSesion.AutenticadoCliente)
        assertEquals("Francisca Tapia Morales", (estadoSesion as EstadoSesion.AutenticadoCliente).cliente.nombre)

        val clienteRepoActualizado = clienteRepo.obtenerPorId(1).first()
        assertEquals("Francisca Tapia Morales", clienteRepoActualizado?.nombre)
    }

    @Test
    fun editarNombre_conNombreVacio_muestraErrorYNoActualiza() = runTest {
        sesionRepo.iniciarSesionDemoCliente()
        val nombreOriginal = (sesionRepo.estadoSesion.value as EstadoSesion.AutenticadoCliente).cliente.nombre

        val viewModel = MiPerfilClienteViewModel(sesionRepo, clienteRepo)
        advanceUntilIdle()

        viewModel.abrirDialogoEdicion()
        viewModel.onNuevoNombreCambiado("   ")
        viewModel.guardarNombre()
        advanceUntilIdle()

        // Debe rechazar el cambio y conservar el nombre previo
        val estadoVm = viewModel.uiState.value
        assertNotNull(estadoVm.errorNombre)
        assertEquals(nombreOriginal, estadoVm.nombre)

        val clienteRepoSinCambio = clienteRepo.obtenerPorId(1).first()
        assertEquals(nombreOriginal, clienteRepoSinCambio?.nombre)
    }

    @Test
    fun editarNombre_conNombreDemasiadoCorto_muestraErrorYNoActualiza() = runTest {
        sesionRepo.iniciarSesionDemoCliente()

        val viewModel = MiPerfilClienteViewModel(sesionRepo, clienteRepo)
        advanceUntilIdle()

        viewModel.abrirDialogoEdicion()
        viewModel.onNuevoNombreCambiado("Jo")
        viewModel.guardarNombre()
        advanceUntilIdle()

        val estadoVm = viewModel.uiState.value
        assertNotNull(estadoVm.errorNombre)
    }

    @Test
    fun cerrarSesion_cambiaEstadoSesionANoAutenticado() = runTest {
        sesionRepo.iniciarSesionDemoCliente()
        assertTrue(sesionRepo.estadoSesion.value is EstadoSesion.AutenticadoCliente)

        val viewModel = MiPerfilClienteViewModel(sesionRepo, clienteRepo)
        advanceUntilIdle()

        var callbackEjecutado = false
        viewModel.cerrarSesion {
            callbackEjecutado = true
        }
        advanceUntilIdle()

        assertTrue(callbackEjecutado)
        assertTrue(sesionRepo.estadoSesion.value is EstadoSesion.NoAutenticado)
    }

    @Test
    fun conteoNoLeidosCliente_sumaCorrectamenteParaInsigniaBarra() = runTest {
        val mensajeRepo = MensajeRepositoryMemoria()
        val clienteId = 1

        // Al inicio comprobamos las conversaciones del cliente
        val convsIniciales = mensajeRepo.obtenerConversacionesCliente(clienteId).first()
        val noLeidosIniciales = convsIniciales.sumOf { it.noLeidosCliente }

        // La artesana envía un mensaje en la conversación 201
        mensajeRepo.enviarMensaje(
            conversacionId = 201,
            contenido = "Hola Francisca, tu pedido está listo.",
            emisor = TipoEmisor.EMPRENDEDORA
        )

        val convsActualizadas = mensajeRepo.obtenerConversacionesCliente(clienteId).first()
        val noLeidosTrasRespuesta = convsActualizadas.sumOf { it.noLeidosCliente }

        assertEquals(noLeidosIniciales + 1, noLeidosTrasRespuesta)
    }
}
