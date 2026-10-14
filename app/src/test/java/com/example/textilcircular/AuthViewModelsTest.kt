package com.example.textilcircular

import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.screens.auth.LoginViewModel
import com.example.textilcircular.ui.screens.auth.RegistroGuiadoViewModel
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
class AuthViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sesionRepository: SesionRepositoryMemoria
    private lateinit var emprendedoraRepository: EmprendedoraRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        emprendedoraRepository = EmprendedoraRepositoryMemoria()
        sesionRepository = SesionRepositoryMemoria(emprendedoraRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginViewModel_validaCamposErroneos() {
        val viewModel = LoginViewModel(sesionRepository)

        var exitoEmprendedora = false
        var exitoAdmin = false

        viewModel.iniciarSesion(
            onExitoEmprendedora = { exitoEmprendedora = true },
            onExitoAdmin = { exitoAdmin = true }
        )

        assertFalse(exitoEmprendedora)
        assertFalse(exitoAdmin)
        assertNotNull(viewModel.uiState.value.errorCorreo)
        assertNotNull(viewModel.uiState.value.errorPassword)
    }

    @Test
    fun loginViewModel_iniciaSesionEmprendedoraConExito() = runTest {
        val viewModel = LoginViewModel(sesionRepository)

        viewModel.onCorreoCambiado("camila.morales@textilcircular.cl")
        viewModel.onPasswordCambiado("clave123")

        var exitoEmprendedora = false
        var exitoAdmin = false

        viewModel.iniciarSesion(
            onExitoEmprendedora = { exitoEmprendedora = true },
            onExitoAdmin = { exitoAdmin = true }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exitoEmprendedora)
        assertFalse(exitoAdmin)
        assertTrue(sesionRepository.estadoSesion.value is EstadoSesion.AutenticadoEmprendedora)
    }

    @Test
    fun loginViewModel_iniciaSesionAdminConExito() = runTest {
        val viewModel = LoginViewModel(sesionRepository)

        viewModel.onCorreoCambiado("admin@textilcircular.cl")
        viewModel.onPasswordCambiado("admin123")

        var exitoAdmin = false

        viewModel.iniciarSesion(
            onExitoEmprendedora = {},
            onExitoAdmin = { exitoAdmin = true }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exitoAdmin)
        assertEquals(EstadoSesion.AutenticadoAdmin, sesionRepository.estadoSesion.value)
    }

    @Test
    fun registroGuiadoViewModel_flujoCompletoDePasos() = runTest {
        val viewModel = RegistroGuiadoViewModel(sesionRepository)

        // Paso 1: Falla si está vacío
        assertFalse(viewModel.avanzarPaso())
        assertEquals(1, viewModel.uiState.value.pasoActual)
        assertNotNull(viewModel.uiState.value.errorNombre)

        // Llenar Paso 1 correctamente
        viewModel.onNombreCompletoCambiado("Catalina Andrea Silva")
        viewModel.onRutCambiado("123456785") // sin formato
        viewModel.onCorreoCambiado("catalina.silva@correo.cl")
        viewModel.onPasswordCambiado("secreto123")

        assertTrue(viewModel.avanzarPaso())
        assertEquals(2, viewModel.uiState.value.pasoActual)
        assertEquals("12.345.678-5", viewModel.uiState.value.rut) // Formateado automáticamente

        // Paso 2: Falla sin nombre comercial o sin rubros
        assertFalse(viewModel.avanzarPaso())
        viewModel.onNombreComercialCambiado("Taller Costuras del Mar")
        viewModel.onRubroToggled(Rubro.SUPRARRECICLAJE)
        viewModel.onRubroToggled(Rubro.REPARACION)

        // Intento de agregar un 3er rubro supera el límite de 2
        viewModel.onRubroToggled(Rubro.SEGUNDA_MANO)
        assertEquals(2, viewModel.uiState.value.rubrosSeleccionados.size)

        assertTrue(viewModel.avanzarPaso())
        assertEquals(3, viewModel.uiState.value.pasoActual)

        // Paso 3: Selección de región y comuna
        viewModel.onRegionSeleccionada("Valparaíso")
        viewModel.onComunaSeleccionada("Viña del Mar")
        viewModel.onInstagramCambiado("costuras_del_mar")

        assertTrue(viewModel.avanzarPaso())
        assertEquals(4, viewModel.uiState.value.pasoActual)

        // Paso 4: Finalizar requiere consentimiento
        var exitoRegistro = false
        viewModel.finalizarRegistro(onExito = { exitoRegistro = true })
        assertFalse(exitoRegistro)
        assertNotNull(viewModel.uiState.value.errorGeneral)

        viewModel.onAceptaLeyDatosCambiado(true)
        viewModel.finalizarRegistro(onExito = { exitoRegistro = true })

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exitoRegistro)
        assertTrue(sesionRepository.estadoSesion.value is EstadoSesion.AutenticadoEmprendedora)
    }
}
