package com.example.textilcircular

import com.example.textilcircular.data.repository.AdminRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.ProductoRepositoryMemoria
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.screens.cliente.ExplorarViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Pruebas unitarias para la exploración del mercado de emprendedoras y filtros de búsqueda
@OptIn(ExperimentalCoroutinesApi::class)
class ExplorarMercadoTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var emprendedoraRepo: EmprendedoraRepositoryMemoria
    private lateinit var productoRepo: ProductoRepositoryMemoria
    private lateinit var adminRepo: AdminRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        emprendedoraRepo = EmprendedoraRepositoryMemoria()
        productoRepo = ProductoRepositoryMemoria()
        adminRepo = AdminRepositoryMemoria(emprendedoraRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun mercado_muestraSoloEmprendedorasActivas() = runTest {
        val viewModel = ExplorarViewModel(emprendedoraRepo, productoRepo, adminRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val lista = viewModel.uiState.value.emprendedoras
        assertEquals(8, lista.size)
        // Ninguna oculta o dada de baja debe aparecer en el mercado
        assertTrue(lista.all { it.emprendedora.activa })
        assertFalse(lista.any { it.emprendedora.id == 9 || it.emprendedora.id == 10 })
    }

    @Test
    fun mercado_filtraPorTextoDeBusqueda() = runTest {
        val viewModel = ExplorarViewModel(emprendedoraRepo, productoRepo, adminRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onQueryTextoChange("Vintage")
        viewModel.ejecutarBusqueda()
        testDispatcher.scheduler.advanceUntilIdle()

        val resultados = viewModel.uiState.value.emprendedoras
        assertTrue(resultados.isNotEmpty())
        assertTrue(resultados.all { it.emprendedora.nombreComercial.contains("Vintage", ignoreCase = true) })
    }

    @Test
    fun mercado_filtraPorRegionYComuna() = runTest {
        val viewModel = ExplorarViewModel(emprendedoraRepo, productoRepo, adminRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onRegionFiltroChange("Valparaíso")
        testDispatcher.scheduler.advanceUntilIdle()

        val enValparaiso = viewModel.uiState.value.emprendedoras
        assertTrue(enValparaiso.all { it.emprendedora.region.equals("Valparaíso", ignoreCase = true) })

        viewModel.onComunaFiltroChange("Valparaíso")
        testDispatcher.scheduler.advanceUntilIdle()

        val enComuna = viewModel.uiState.value.emprendedoras
        assertEquals(1, enComuna.size)
        assertTrue(enComuna.all { it.emprendedora.comuna.equals("Valparaíso", ignoreCase = true) })
    }

    @Test
    fun mercado_filtraPorRubro() = runTest {
        val viewModel = ExplorarViewModel(emprendedoraRepo, productoRepo, adminRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onRubroFiltroChange(Rubro.SUPRARRECICLAJE.nombre)
        testDispatcher.scheduler.advanceUntilIdle()

        val resultados = viewModel.uiState.value.emprendedoras
        assertTrue(resultados.isNotEmpty())
        assertTrue(resultados.all { it.emprendedora.rubros.contains(Rubro.SUPRARRECICLAJE) })
    }

    @Test
    fun mercado_limpiarFiltrosRestauraListaCompleta() = runTest {
        val viewModel = ExplorarViewModel(emprendedoraRepo, productoRepo, adminRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onRegionFiltroChange("Valparaíso")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.emprendedoras.size)

        viewModel.limpiarFiltros()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(8, viewModel.uiState.value.emprendedoras.size)
        assertEquals("", viewModel.uiState.value.regionFiltro)
        assertEquals("", viewModel.uiState.value.comunaFiltro)
    }

    @Test
    fun mercado_registraBusquedaEnMetricasAdmin() = runTest {
        val metricasIniciales = adminRepo.obtenerMetricas().first()
        val busquedasIniciales = metricasIniciales.totalBusquedas

        val viewModel = ExplorarViewModel(emprendedoraRepo, productoRepo, adminRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onQueryTextoChange("bolso")
        viewModel.ejecutarBusqueda()
        testDispatcher.scheduler.advanceUntilIdle()

        val metricasDespues = adminRepo.obtenerMetricas().first()
        assertEquals(busquedasIniciales + 1, metricasDespues.totalBusquedas)
    }
}
