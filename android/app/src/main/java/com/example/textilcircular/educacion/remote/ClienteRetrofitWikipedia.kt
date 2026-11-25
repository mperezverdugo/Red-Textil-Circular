package com.example.textilcircular.educacion.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Instancia aislada de Retrofit para consumir la API de Wikipedia sin acoplarse a los microservicios del proyecto.
object ClienteRetrofitWikipedia {

    private const val BASE_URL = "https://es.wikipedia.org/api/rest_v1/"

    // Identificador exigido por las políticas de la Fundación Wikimedia para clientes de API
    private const val USER_AGENT = "TextilCircularApp/1.0 (contacto@textilcircular.cl; https://github.com/mperezverdugo/Red-Textil-Circular)"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val userAgentInterceptor = Interceptor { chain ->
            val requestOriginal = chain.request()
            val requestConCabecera = requestOriginal.newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()
            chain.proceed(requestConCabecera)
        }

        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val servicio: WikipediaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikipediaApiService::class.java)
    }
}
