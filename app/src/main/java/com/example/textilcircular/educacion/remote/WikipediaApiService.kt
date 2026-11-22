package com.example.textilcircular.educacion.remote

import retrofit2.http.GET
import retrofit2.http.Path

// Servicio Retrofit para consumir la API REST pública de Wikipedia en español.
interface WikipediaApiService {

    @GET("page/summary/{title}")
    suspend fun obtenerResumenArticulo(
        @Path("title") titulo: String
    ): WikipediaSummaryDto
}
