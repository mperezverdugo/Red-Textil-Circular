package com.example.textilcircular

import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.ProductoRepositoryMemoria
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.ui.screens.catalog.CatalogoViewModel
import com.example.textilcircular.ui.screens.catalog.ProductoFormViewModel
import com.example.textilcircular.util.Constantes
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
class CatalogoViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var productoRepository: ProductoRepositoryMemoria
    private lateinit var sesionRepository: SesionRepositoryMemoria
    private lateinit var emprendedoraRepository: EmprendedoraRepositoryMemoria

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        emprendedoraRepository = EmprendedoraRepositoryMemoria()
        sesionRepository = SesionRepositoryMemoria(emprendedoraRepository)
        productoRepository = ProductoRepositoryMemoria()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun catalogoViewModel_cargaProductosCorrectamente() = runTest {
        sesionRepository.iniciarSesionDemoEmprendedora()
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = CatalogoViewModel(productoRepository, sesionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.estaCargando)
        assertNull(uiState.error)
        assertTrue(uiState.productos.isNotEmpty())
    }

    @Test
    fun productoFormViewModel_cargaProductoExistente() = runTest {
        val prodDemo = com.example.textilcircular.ui.preview.DatosDemo.ProductosDemo.first()
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)
        viewModel.cargarProducto(prodDemo.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(prodDemo.id, uiState.id)
        assertTrue(uiState.nombre.isNotEmpty())
        assertTrue(uiState.descripcionCorta.isNotEmpty())
    }

    @Test
    fun productoFormViewModel_actualizaCamposDeTexto() = runTest {
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)

        viewModel.onNombreCambiado("Nuevo Bolso")
        viewModel.onDescripcionCambiado("Descripción con material reciclado")
        viewModel.onExplicacionProcesoCambiado("Técnica de corte y unión manual")

        val uiState = viewModel.uiState.value
        assertEquals("Nuevo Bolso", uiState.nombre)
        assertEquals("Descripción con material reciclado", uiState.descripcionCorta)
        assertEquals("Técnica de corte y unión manual", uiState.explicacionProceso)
    }

    @Test
    fun productoFormViewModel_auditoriaLimites_rechazaNombreMuyCorto() = runTest {
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)
        viewModel.onNombreCambiado("A") // Menos del mínimo de 3 caracteres
        viewModel.onDescripcionCambiado("Descripción de longitud válida para el producto")

        var guardadoExitoso = false
        viewModel.guardarProducto(onExito = { guardadoExitoso = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(guardadoExitoso)
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.errorNombre)
    }

    @Test
    fun productoFormViewModel_auditoriaLimites_rechazaDescripcionMuyCorta() = runTest {
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)
        viewModel.onNombreCambiado("Bolso Reciclado")
        viewModel.onDescripcionCambiado("Corta") // Menos del mínimo de 10 caracteres

        var guardadoExitoso = false
        viewModel.guardarProducto(onExito = { guardadoExitoso = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(guardadoExitoso)
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.errorDescripcion)
    }

    @Test
    fun productoFormViewModel_guardaProductoValido() = runTest {
        sesionRepository.iniciarSesionDemoEmprendedora()
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)
        viewModel.onNombreCambiado("Bolso Ecológico Jeans")
        viewModel.onDescripcionCambiado("Bolso confeccionado con mezclilla recuperada")
        viewModel.onExplicacionProcesoCambiado("Técnica de upcycling de retazos")

        var guardadoExitoso = false
        viewModel.guardarProducto(onExito = { guardadoExitoso = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(guardadoExitoso)
        val uiState = viewModel.uiState.value
        assertNull(uiState.errorGeneral)
    }

    @Test
    fun productoFormViewModel_eliminaProductoExistente() = runTest {
        val prodDemo = com.example.textilcircular.ui.preview.DatosDemo.ProductosDemo.first()
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)
        viewModel.cargarProducto(prodDemo.id)
        testDispatcher.scheduler.advanceUntilIdle()

        var eliminadoExitoso = false
        viewModel.eliminarProducto(onExito = { eliminadoExitoso = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(eliminadoExitoso)
    }

    @Test
    fun productoFormViewModel_manejoPermisosCamara() = runTest {
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)

        // Denegado con posibilidad de explicar
        viewModel.onPermisoCamaraDenegado(noVolverAPreguntar = false)
        assertTrue(viewModel.uiState.value.mostrarExplicacionPermisoCamara)
        assertFalse(viewModel.uiState.value.mostrarDialogoIrAAjustes)

        viewModel.cerrarDialogoExplicacionPermiso()
        assertFalse(viewModel.uiState.value.mostrarExplicacionPermisoCamara)

        // Denegado con 'no volver a preguntar'
        viewModel.onPermisoCamaraDenegado(noVolverAPreguntar = true)
        assertTrue(viewModel.uiState.value.mostrarDialogoIrAAjustes)
        assertFalse(viewModel.uiState.value.mostrarExplicacionPermisoCamara)

        viewModel.cerrarDialogoAjustes()
        assertFalse(viewModel.uiState.value.mostrarDialogoIrAAjustes)
    }

    @Test
    fun productoFormViewModel_eliminaFotoDeLista() = runTest {
        val prodDemo = com.example.textilcircular.ui.preview.DatosDemo.ProductosDemo.first()
        val viewModel = ProductoFormViewModel(productoRepository, sesionRepository)
        viewModel.cargarProducto(prodDemo.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val totalInicial = viewModel.uiState.value.fotos.size
        if (totalInicial > 0) {
            viewModel.eliminarFoto(0)
            assertEquals(totalInicial - 1, viewModel.uiState.value.fotos.size)
        }
    }
}
