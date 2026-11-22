package com.example.textilcircular.data.remote.api

import com.example.textilcircular.data.remote.dto.ConversacionResponseDto
import com.example.textilcircular.data.remote.dto.CrearConversacionRequestDto
import com.example.textilcircular.data.remote.dto.CrearProductoRequestDto
import com.example.textilcircular.data.remote.dto.EditarProductoRequestDto
import com.example.textilcircular.data.remote.dto.EnviarMensajeRequestDto
import com.example.textilcircular.data.remote.dto.MensajeResponseDto
import com.example.textilcircular.data.remote.dto.ProductoResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Interfaz Retrofit para el microservicio ms-mercado (puerto 8082).
interface MercadoApi {

    // Endpoints de Productos
    @GET("productos")
    suspend fun listarPorEmprendedora(
        @Query("emprendedoraId") emprendedoraId: Long
    ): Response<List<ProductoResponseDto>>

    @GET("productos/mios")
    suspend fun listarMios(
        @Header("Authorization") autorizacion: String? = null
    ): Response<List<ProductoResponseDto>>

    @GET("productos/{id}")
    suspend fun obtenerProducto(
        @Path("id") id: Long
    ): Response<ProductoResponseDto>

    @POST("productos")
    suspend fun crearProducto(
        @Header("Authorization") autorizacion: String? = null,
        @Body solicitud: CrearProductoRequestDto
    ): Response<ProductoResponseDto>

    @PUT("productos/{id}")
    suspend fun editarProducto(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long,
        @Body solicitud: EditarProductoRequestDto
    ): Response<ProductoResponseDto>

    @DELETE("productos/{id}")
    suspend fun eliminarProducto(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long
    ): Response<Unit>

    // Endpoints de Conversaciones y Chat
    @POST("conversaciones")
    suspend fun crearOObtenerConversacion(
        @Header("Authorization") autorizacion: String? = null,
        @Body solicitud: CrearConversacionRequestDto
    ): Response<ConversacionResponseDto>

    @GET("conversaciones")
    suspend fun listarConversaciones(
        @Header("Authorization") autorizacion: String? = null
    ): Response<List<ConversacionResponseDto>>

    @POST("conversaciones/{id}/mensajes")
    suspend fun enviarMensaje(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long,
        @Body solicitud: EnviarMensajeRequestDto
    ): Response<MensajeResponseDto>

    @GET("conversaciones/{id}/mensajes")
    suspend fun listarMensajes(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long,
        @Query("despuesDeId") despuesDeId: Long? = null
    ): Response<List<MensajeResponseDto>>

    @PATCH("conversaciones/{id}/lectura")
    suspend fun marcarLectura(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long
    ): Response<ConversacionResponseDto>
}
