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

    @GET("usuarios/me")
    suspend fun obtenerMiPerfil(
        @Header("Authorization") autorizacion: String? = null
    ): Response<PerfilResponseDto>

    @GET("admin/emprendedoras")
    suspend fun listarEmprendedorasAdmin(
        @Header("Authorization") autorizacion: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<EmprendedoraAdminDto>>

    @GET("admin/emprendedoras/{id}")
    suspend fun obtenerEmprendedoraAdmin(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long
    ): Response<EmprendedoraAdminDto>

    @POST("admin/emprendedoras")
    suspend fun crearEmprendedoraAdmin(
        @Header("Authorization") autorizacion: String? = null,
        @Body solicitud: RegistroEmprendedoraRequestDto
    ): Response<EmprendedoraAdminDto>

    @PUT("admin/emprendedoras/{id}")
    suspend fun editarEmprendedoraAdmin(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long,
        @Body solicitud: EditarFichaAdminRequestDto
    ): Response<EmprendedoraAdminDto>

    @DELETE("admin/emprendedoras/{id}")
    suspend fun eliminarEmprendedoraAdmin(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long
    ): Response<Unit>

    @PATCH("admin/emprendedoras/{id}/revision")
    suspend fun revisarEmprendedora(
        @Header("Authorization") autorizacion: String? = null,
        @Path("id") id: Long,
        @Body solicitud: RevisionFichaRequestDto
    ): Response<RevisionFichaResponseDto>
}
