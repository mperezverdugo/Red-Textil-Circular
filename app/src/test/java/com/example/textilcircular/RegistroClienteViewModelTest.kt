package com.example.textilcircular

import com.example.textilcircular.data.repository.ClienteRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.ui.screens.cliente.RegistroClienteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroClienteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sesionRepository: SesionRepositoryMemoria
    private lateinit var clienteRepository: ClienteRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        clienteRepository = ClienteRepositoryMemoria()
        val emprendedoraRepository = EmprendedoraRepositoryMemoria()
        sesionRepository = SesionRepositoryMemoria(emprendedoraRepository, clienteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registrar_detectaCamposVaciosOInvalidos() {
        val viewModel = RegistroClienteViewModel(sesionRepository)

        var llamado = false
        viewModel.registrar(onExito = { llamado = true })

        assertFalse(llamado)
        assertNotNull(viewModel.uiState.value.errorNombre)
        assertNotNull(viewModel.uiState.value.errorCorreo)
        assertNotNull(viewModel.uiState.value.errorPassword)
    }

    @Test
    fun registrar_exigeAceptacionDeLeyDeDatos() {
        val viewModel = RegistroClienteViewModel(sesionRepository)

        viewModel.onNombreCambiado("Francisca Tapia")
        viewModel.onCorreoCambiado("francisca.nueva@correo.cl")
        viewModel.onPasswordCambiado("claveSegura123")
        viewModel.onAceptaLeyDatosCambiado(false)

        var llamado = false
        viewModel.registrar(onExito = { llamado = true })

        assertFalse(llamado)
        assertNotNull(viewModel.uiState.value.errorGeneral)
    }

    @Test
    fun registrar_completaRegistroConDatosValidos() = runTest {
        val viewModel = RegistroClienteViewModel(sesionRepository)

        viewModel.onNombreCambiado("Francisca Tapia")
        viewModel.onCorreoCambiado("francisca.nueva@correo.cl")
        viewModel.onPasswordCambiado("claveSegura123")
        viewModel.onAceptaLeyDatosCambiado(true)

        var exitoso = false
        viewModel.registrar(onExito = { exitoso = true })

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exitoso)
        assertNull(viewModel.uiState.value.errorGeneral)
        assertTrue(sesionRepository.estadoSesion.value is EstadoSesion.AutenticadoCliente)
        val clienteSesion = (sesionRepository.estadoSesion.value as EstadoSesion.AutenticadoCliente).cliente
        assertEquals("Francisca Tapia", clienteSesion.nombre)
        assertEquals("francisca.nueva@correo.cl", clienteSesion.email)
    }

    @Test
    fun registrar_muestraErrorSiCorreoYaExiste() = runTest {
        val viewModel = RegistroClienteViewModel(sesionRepository)

        // Usamos el correo ya existente en DatosDemo
        viewModel.onNombreCambiado("Francisca Tapia")
        viewModel.onCorreoCambiado("francisca.tapia@correo.cl")
        viewModel.onPasswordCambiado("claveSegura123")
        viewModel.onAceptaLeyDatosCambiado(true)

        var exitoso = false
        viewModel.registrar(onExito = { exitoso = true })

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(exitoso)
        assertNotNull(viewModel.uiState.value.errorGeneral)
    }
}
