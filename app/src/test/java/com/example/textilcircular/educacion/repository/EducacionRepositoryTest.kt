package com.example.textilcircular.educacion.repository

import com.example.textilcircular.educacion.remote.WikipediaApiService
import com.example.textilcircular.educacion.remote.WikipediaContentUrlsDto
import com.example.textilcircular.educacion.remote.WikipediaPageUrlDto
import com.example.textilcircular.educacion.remote.WikipediaSummaryDto
import com.example.textilcircular.educacion.remote.WikipediaThumbnailDto
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class EducacionRepositoryTest {

    private class FakeWikipediaApiService(
        private val responderConError: Boolean = false
    ) : WikipediaApiService {
        override suspend fun obtenerResumenArticulo(titulo: String): WikipediaSummaryDto {
            if (responderConError) {
                throw IOException("Simulated network timeout")
            }
            return WikipediaSummaryDto(
                title = titulo.replace("_", " "),
                displayTitle = "<b>${titulo.replace("_", " ")}</b>",
                extractHtml = "<p>Extracto formativo sobre <b>$titulo</b> para la industria circular.</p>",
                thumbnail = WikipediaThumbnailDto(
                    source = "https://upload.wikimedia.org/test.jpg",
                    width = 200,
                    height = 200
                ),
                contentUrls = WikipediaContentUrlsDto(
                    mobile = WikipediaPageUrlDto(page = "https://es.m.wikipedia.org/wiki/$titulo")
                )
            )
        }
    }

    @Test
    fun obtenerArticuloPorTitulo_exitoso_limpiaHtmlYMapeaDatosCorrectamente() = runTest {
        val fakeService = FakeWikipediaApiService(responderConError = false)
        val repository = EducacionRepositoryWikipedia(fakeService)

        val resultado = repository.obtenerArticuloPorTitulo("Reciclaje_textil", "Gestión de Residuos")

        assertTrue("El resultado debe ser Exito", resultado is Resultado.Exito)
        val articulo = (resultado as Resultado.Exito).datos
        assertEquals("Reciclaje textil", articulo.titulo)
        assertEquals("Extracto formativo sobre Reciclaje_textil para la industria circular.", articulo.extracto)
        assertEquals("https://upload.wikimedia.org/test.jpg", articulo.imagenUrl)
        assertEquals("https://es.m.wikipedia.org/wiki/Reciclaje_textil", articulo.urlOriginal)
        assertEquals("Gestión de Residuos", articulo.etiquetaTematica)
    }

    @Test
    fun obtenerArticuloPorTitulo_errorDeRed_retornaResultadoError() = runTest {
        val fakeService = FakeWikipediaApiService(responderConError = true)
        val repository = EducacionRepositoryWikipedia(fakeService)

        val resultado = repository.obtenerArticuloPorTitulo("Economía_circular")

        assertTrue("El resultado debe ser Error al fallar la red", resultado is Resultado.Error)
        val mensaje = (resultado as Resultado.Error).mensaje
        assertTrue(mensaje.contains("Error al consultar el artículo"))
        assertNotNull(resultado.excepcion)
    }

    @Test
    fun obtenerTemasEducativos_exitoso_obtieneListaConTodosLosTemasFijos() = runTest {
        val fakeService = FakeWikipediaApiService(responderConError = false)
        val repository = EducacionRepositoryWikipedia(fakeService)

        val resultado = repository.obtenerTemasEducativos()

        assertTrue("Debe retornar Exito", resultado is Resultado.Exito)
        val articulos = (resultado as Resultado.Exito).datos
        assertEquals(EducacionRepositoryWikipedia.TEMAS_FIJOS.size, articulos.size)
    }
}
