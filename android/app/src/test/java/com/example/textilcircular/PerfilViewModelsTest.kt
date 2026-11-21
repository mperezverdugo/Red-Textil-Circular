package com.example.textilcircular

import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.data.repository.SincronizacionRepositoryMemoria
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.screens.profile.PerfilViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sesionRepository: SesionRepositoryMemoria
    private lateinit var emprendedoraRepository: EmprendedoraRepositoryMemoria
    private lateinit var sincronizacionRepository: SincronizacionRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        emprendedoraRepository = EmprendedoraRepositoryMemoria()
        sesionRepository = SesionRepositoryMemoria(emprendedoraRepository)
        sincronizacionRepository = SincronizacionRepositoryMemoria()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun perfilViewModel_cargaDatosInicialesDeSesion() = runTest {
        sesionRepository.iniciarSesionDemoEmprendedora()
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("Camila Andrea Morales Soto", uiState.emprendedora.nombre)
        assertTrue(uiState.itemsPendientes.isEmpty())
    }

    @Test
    fun perfilViewModel_editaPerfilConExito() = runTest {
        sesionRepository.iniciarSesionDemoEmprendedora()
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.prepararEdicion()
        viewModel.onEditNombreCambiado("Camila Morales Soto")
        viewModel.onEditNombreComercialCambiado("Hilos Renovados")
        viewModel.onEditRegionSeleccionada("Valparaíso")
        viewModel.onEditComunaSeleccionada("Quilpué")
        viewModel.onEditInstagramCambiado("hilos_renovados")

        var guardadoExitoso = false
        viewModel.guardarCambiosPerfil(onExito = { guardadoExitoso = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(guardadoExitoso)

        val sesionActual = sesionRepository.estadoSesion.value
        assertTrue(sesionActual is EstadoSesion.AutenticadoEmprendedora)
        val emp = (sesionActual as EstadoSesion.AutenticadoEmprendedora).emprendedora
        assertEquals("Hilos Renovados", emp.nombreComercial)
        assertEquals("Quilpué", emp.comuna)
    }

    @Test
    fun perfilViewModel_validaErroresDeEdicion() = runTest {
        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)

        viewModel.prepararEdicion()
        viewModel.onEditNombreCambiado("") // Vacío

        var guardadoExitoso = false
        viewModel.guardarCambiosPerfil(onExito = { guardadoExitoso = true })

        assertFalse(guardadoExitoso)
        assertNotNull(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun perfilViewModel_fuerzaSincronizacion() = runTest {
        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.forzarSincronizacion()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(EstadoSincronizacion.SINCRONIZADO, viewModel.uiState.value.estadoSincronizacion)
        assertTrue(viewModel.uiState.value.itemsPendientes.isEmpty())
    }

    @Test
    fun perfilViewModel_solicitaBajaVoluntaria() = runTest {
        sesionRepository.iniciarSesionDemoEmprendedora()
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        var bajaExitosa = false
        viewModel.solicitarBajaVoluntaria(onExito = { bajaExitosa = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(bajaExitosa)
        assertEquals(EstadoSesion.NoAutenticado, sesionRepository.estadoSesion.value)
    }

    @Test
    fun perfilViewModel_alternaModoSinConexion() {
        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)

        assertFalse(viewModel.uiState.value.modoSinConexion)
        viewModel.onModoSinConexionCambiado(true)
        assertTrue(viewModel.uiState.value.modoSinConexion)
    }

    @Test
    fun perfilViewModel_alternaSimularListasVacias() {
        val viewModel = PerfilViewModel(sesionRepository, emprendedoraRepository, sincronizacionRepository)

        assertFalse(viewModel.uiState.value.simularListasVacias)
        viewModel.onSimularListasVaciasCambiado(true)
        assertTrue(viewModel.uiState.value.simularListasVacias)
    }
}
