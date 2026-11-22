package com.example.textilcircular.data.remote.api

import com.example.textilcircular.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

// Interfaz Retrofit para el microservicio ms-usuarios (puerto 8081).
interface UsuariosApi {

    @POST("auth/login")
    suspend fun login(@Body solicitud: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/registro/cliente")
    suspend fun registrarCliente(@Body solicitud: RegistroClienteRequestDto): Response<UsuarioDto>

    @POST("auth/registro/emprendedora")
    suspend fun registrarEmprendedora(@Body solicitud: RegistroEmprendedoraRequestDto): Response<RegistroEmprendedoraResponseDto>

    @GET("emprendedoras")
    suspend fun listarEmprendedoras(
        @Header("Authorization") autorizacion: String? = null,
        @Query("texto") texto: String? = null,
        @Query("region") region: String? = null,
        @Query("comuna") comuna: String? = null,
        @Query("rubro") rubro: String? = null
    ): Response<List<EmprendedoraPublicaDto>>

    @GET("emprendedoras/{id}")
    suspend fun obtenerEmprendedora(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long
    ): Response<EmprendedoraPublicaDto>
}
