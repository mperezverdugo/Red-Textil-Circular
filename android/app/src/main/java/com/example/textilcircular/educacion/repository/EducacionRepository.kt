package com.example.textilcircular.educacion.repository

import com.example.textilcircular.educacion.model.ArticuloEducativo
import com.example.textilcircular.educacion.remote.ClienteRetrofitWikipedia
import com.example.textilcircular.educacion.remote.WikipediaApiService
import com.example.textilcircular.educacion.remote.WikipediaSummaryDto
import com.example.textilcircular.educacion.util.LimpiadorHtml
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Contrato para la obtención de contenidos educativos desde la API externa.
interface EducacionRepository {
    suspend fun obtenerTemasEducativos(): Resultado<List<ArticuloEducativo>>
    suspend fun obtenerArticuloPorTitulo(titulo: String, etiqueta: String = "Educación Textil"): Resultado<ArticuloEducativo>
}

// Implementación del repositorio consumiendo Wikipedia REST API mediante Retrofit.
class EducacionRepositoryWikipedia(
    private val apiService: WikipediaApiService = ClienteRetrofitWikipedia.servicio
) : EducacionRepository {

    // Temas fijos clave vinculados al propósito de Red Textil Circular
    companion object {
        val TEMAS_FIJOS = listOf(
            "Reciclaje_textil" to "Reciclaje y Gestión de Residuos",
            "Economía_circular" to "Economía Circular y Sostenibilidad",
            "Suprarreciclaje" to "Upcycling y Creación de Valor",
            "Moda_sostenible" to "Moda Ética y Consumo Responsable"
        )
    }

    override suspend fun obtenerTemasEducativos(): Resultado<List<ArticuloEducativo>> = withContext(Dispatchers.IO) {
        val listaArticulos = mutableListOf<ArticuloEducativo>()
        var erroresConsecutivos = 0

        for ((titulo, etiqueta) in TEMAS_FIJOS) {
            when (val res = obtenerArticuloPorTitulo(titulo, etiqueta)) {
                is Resultado.Exito -> listaArticulos.add(res.datos)
                is Resultado.Error -> erroresConsecutivos++
            }
        }

        if (listaArticulos.isNotEmpty()) {
            Resultado.Exito(listaArticulos)
        } else {
            Resultado.Error("No fue posible cargar contenidos educativos desde Wikipedia. Comprueba tu conexión a internet.")
        }
    }

    override suspend fun obtenerArticuloPorTitulo(
        titulo: String,
        etiqueta: String
    ): Resultado<ArticuloEducativo> = withContext(Dispatchers.IO) {
        try {
            val dto = apiService.obtenerResumenArticulo(titulo)
            val articulo = mapearDtoAArticulo(dto, claveDefecto = titulo, etiqueta = etiqueta)
            Resultado.Exito(articulo)
        } catch (e: Exception) {
            Resultado.Error("Error al consultar el artículo '$titulo' en Wikipedia: ${e.localizedMessage ?: e.message}", e)
        }
    }

    private fun mapearDtoAArticulo(
        dto: WikipediaSummaryDto,
        claveDefecto: String,
        etiqueta: String
    ): ArticuloEducativo {
        val tituloLimpio = LimpiadorHtml.limpiar(dto.displayTitle ?: dto.title ?: claveDefecto.replace("_", " "))
        
        // Priorizar extract_html para sanitizar o el extract plano
        val extractoCrudo = dto.extractHtml ?: dto.extract ?: dto.description ?: "Sin descripción disponible."
        val extractoLimpio = LimpiadorHtml.limpiar(extractoCrudo)

        val urlPagina = dto.contentUrls?.mobile?.page
            ?: dto.contentUrls?.desktop?.page
            ?: "https://es.wikipedia.org/wiki/$claveDefecto"

        val imagen = dto.thumbnail?.source ?: dto.originalImage?.source

        return ArticuloEducativo(
            clave = claveDefecto,
            titulo = tituloLimpio,
            extracto = extractoLimpio,
            imagenUrl = imagen,
            urlOriginal = urlPagina,
            etiquetaTematica = etiqueta
        )
    }
}
