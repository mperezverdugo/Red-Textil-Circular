package com.example.textilcircular.educacion.ui

import com.example.textilcircular.educacion.model.ArticuloEducativo
import com.example.textilcircular.educacion.repository.EducacionRepository
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EducacionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeEducacionRepository(
        var debeFallar: Boolean = false
    ) : EducacionRepository {
        val articulosPrueba = listOf(
            ArticuloEducativo(
                clave = "Reciclaje_textil",
                titulo = "Reciclaje textil",
                extracto = "Proceso de recuperación de telas.",
                imagenUrl = "https://example.com/reciclaje.jpg",
                urlOriginal = "https://es.wikipedia.org/wiki/Reciclaje_textil",
                etiquetaTematica = "Reciclaje"
            )
        )

        override suspend fun obtenerTemasEducativos(): Resultado<List<ArticuloEducativo>> {
            return if (debeFallar) {
                Resultado.Error("Error simulado de conexión")
            } else {
                Resultado.Exito(articulosPrueba)
            }
        }

        override suspend fun obtenerArticuloPorTitulo(
            titulo: String,
            etiqueta: String
        ): Resultado<ArticuloEducativo> {
            return if (debeFallar) {
                Resultado.Error("Error simulado")
            } else {
                Resultado.Exito(articulosPrueba.first())
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_cargaExitosa_emiteEstadoExitoConArticulos() = runTest(testDispatcher) {
        val repo = FakeEducacionRepository(debeFallar = false)
        val viewModel = EducacionViewModel(repo)

        // Inicialmente arranca en Cargando
        assertEquals(EducacionUiState.Cargando, viewModel.uiState.value)

        // Ejecutar las corrutinas pendientes
        advanceUntilIdle()

        assertTrue("El estado debe ser Exito", viewModel.uiState.value is EducacionUiState.Exito)
        val estado = viewModel.uiState.value as EducacionUiState.Exito
        assertEquals(1, estado.articulos.size)
        assertEquals("Reciclaje textil", estado.articulos.first().titulo)
    }

    @Test
    fun init_errorEnRepositorio_emiteEstadoErrorConMensaje() = runTest(testDispatcher) {
        val repo = FakeEducacionRepository(debeFallar = true)
        val viewModel = EducacionViewModel(repo)

        advanceUntilIdle()

        assertTrue("El estado debe ser Error", viewModel.uiState.value is EducacionUiState.Error)
        val estado = viewModel.uiState.value as EducacionUiState.Error
        assertEquals("Error simulado de conexión", estado.mensaje)
    }

    @Test
    fun reintentar_trasFallo_recuperaEstadoExito() = runTest(testDispatcher) {
        val repo = FakeEducacionRepository(debeFallar = true)
        val viewModel = EducacionViewModel(repo)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is EducacionUiState.Error)

        // Se corrige el fallo y se llama a reintentar
        repo.debeFallar = false
        viewModel.reintentar()
        advanceUntilIdle()

        assertTrue("El estado debe actualizar a Exito tras reintentar", viewModel.uiState.value is EducacionUiState.Exito)
    }
}
