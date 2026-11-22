package com.example.textilcircular.data.remote

import com.example.textilcircular.data.remote.api.UsuariosApi
import com.example.textilcircular.data.remote.dto.ErrorResponseDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Configuración centralizada de clientes HTTP Retrofit para los microservicios.
object ClienteRetrofit {

    // URL base para el emulador Android (10.0.2.2 apunta al localhost de la máquina anfitriona).
    // En pruebas unitarias o de integración se puede reconfigurar dinámicamente.
    var urlBaseUsuarios: String = "http://10.0.2.2:8081/"

    // Almacén en memoria del token JWT activo para adjuntarlo a las peticiones protegidas
    @Volatile
    var tokenActivo: String? = null

    private val gson = Gson()

    private val interceptorAutenticacion = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        val token = tokenActivo
        if (token != null && original.header("Authorization") == null) {
            builder.header("Authorization", "Bearer $token")
        }
        chain.proceed(builder.build())
    }

    private val interceptorLogging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(interceptorAutenticacion)
            .addInterceptor(interceptorLogging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Instancia de UsuariosApi lista para consumir el microservicio ms-usuarios
    val usuariosApi: UsuariosApi by lazy {
        crearUsuariosApi(urlBaseUsuarios, okHttpClient)
    }

    fun crearUsuariosApi(urlBase: String, client: OkHttpClient = okHttpClient): UsuariosApi {
        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApi::class.java)
    }

    // Extrae el mensaje de error estructurado {codigo, mensaje} devuelto por el microservicio
    fun extraerMensajeError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Error inesperado en el servidor"
        return try {
            val error = gson.fromJson(errorBody, ErrorResponseDto::class.java)
            error.mensaje.ifBlank { "Error en el servidor: ${error.codigo}" }
        } catch (_: Exception) {
            "Error en la comunicación con el servidor"
        }
    }
}
