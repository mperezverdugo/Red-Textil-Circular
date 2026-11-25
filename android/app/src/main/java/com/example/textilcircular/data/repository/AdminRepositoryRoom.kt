package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.AdminDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.data.remote.ClienteRetrofit
import com.example.textilcircular.data.remote.api.UsuariosApi
import com.example.textilcircular.data.remote.dto.EditarFichaAdminRequestDto
import com.example.textilcircular.data.remote.dto.RegistroEmprendedoraRequestDto
import com.example.textilcircular.data.remote.dto.RevisionFichaRequestDto
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.RegistroAutomatizacionCorreo
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.AuditorFichas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Implementación del repositorio de administración respaldado en SQLite y sincronizado con ms-usuarios.
class AdminRepositoryRoom(
    private val adminDao: AdminDao,
    private val emprendedoraDao: EmprendedoraDao,
    private val usuariosApi: UsuariosApi = ClienteRetrofit.usuariosApi,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AdminRepository {

    // Sincroniza todas las fichas desde ms-usuarios (/admin/emprendedoras) hacia Room
    override suspend fun sincronizarFichas(): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (token.isNullOrBlank()) {
            return@withContext Resultado.Error("Se requiere una sesión de administradora activa con token")
        }
        try {
            val respuesta = usuariosApi.listarEmprendedorasAdmin(autorizacion = "Bearer $token")
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val entidades = respuesta.body()!!.map { dto ->
                    val emp = dto.aModelo()
                    val problemas = AuditorFichas.auditar(emp)
                    EmprendedoraEntity.desdeModelo(
                        emprendedora = emp.copy(
                            activa = dto.activa && dto.aEstadoFicha() == EstadoFichaEmprendimiento.ACTIVA
                        ),
                        estadoFicha = dto.aEstadoFicha(),
                        problemas = problemas,
                        motivoRevision = dto.motivoRevision
                    )
                }
                emprendedoraDao.eliminarTodas()
                emprendedoraDao.insertarVarias(entidades)
                Resultado.Exito(Unit)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(errorMsg)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resultado.Error("Error al sincronizar fichas desde el servidor: ${e.message}")
        }
    }

    // Retorna todas las fichas para moderación administrativa, refrescando desde ms-usuarios si hay sesión
    override fun obtenerFichas(): Flow<List<FichaAdminEmprendimiento>> = flow {
        if (!ClienteRetrofit.tokenActivo.isNullOrBlank()) {
            sincronizarFichas()
        }
        adminDao.obtenerFichas().collect { lista ->
            emit(lista.map { it.aFichaAdmin() })
        }
    }

    // Retorna una ficha específica con su diagnóstico de problemas, consultando primero al servidor si hay sesión
    override fun obtenerFichaPorId(id: Int): Flow<FichaAdminEmprendimiento?> = flow {
        val token = ClienteRetrofit.tokenActivo
        if (!token.isNullOrBlank()) {
            try {
                val respuesta = usuariosApi.obtenerEmprendedoraAdmin(
                    autorizacion = "Bearer $token",
                    id = id.toLong()
                )
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val dto = respuesta.body()!!
                    val emp = dto.aModelo()
                    val entidad = EmprendedoraEntity.desdeModelo(
                        emprendedora = emp,
                        estadoFicha = dto.aEstadoFicha(),
                        problemas = AuditorFichas.auditar(emp),
                        motivoRevision = dto.motivoRevision
                    )
                    emprendedoraDao.insertar(entidad)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        adminDao.obtenerFichaPorId(id).collect { emit(it?.aFichaAdmin()) }
    }

    // Calcula de forma reactiva las métricas de impacto a partir de las tablas de SQLite
    override fun obtenerMetricas(): Flow<MetricasAdmin> {
        val conteosBasicosFlow = combine(
            adminDao.contarEventos("BUSQUEDA"),
            adminDao.contarEventos("CONSULTA_ENVIADA"),
            adminDao.contarEventos("CLIC_CONTACTO"),
            adminDao.contarFichasPorRevisar()
        ) { busquedas, consultas, clics, porRevisar ->
            listOf(busquedas, consultas, clics, porRevisar)
        }

        return combine(
            conteosBasicosFlow,
            adminDao.contarContactosPorRegion(),
            adminDao.contarContactosPorRubro()
        ) { basicos, regiones, rubros ->
            MetricasAdmin(
                totalBusquedas = basicos[0],
                totalConsultas = basicos[1],
                totalClics = basicos[2],
                fichasPorRevisar = basicos[3],
                contactosPorRegion = regiones.map { it.nombre to it.total },
                contactosPorRubro = rubros.map { it.nombre to it.total }
            )
        }
    }

    // Registros de los correos automáticos emitidos a emprendedoras
    override fun obtenerCorreos(): Flow<List<RegistroAutomatizacionCorreo>> {
        return adminDao.obtenerCorreos().map { lista ->
            lista.map { it.aModelo() }
        }
    }

    // Registra una búsqueda cuando un cliente aplica filtros en el mercado
    override fun registrarBusqueda(termino: String, region: String?, rubro: String?) {
        scope.launch {
            adminDao.registrarEvento(
                MetricaEventoEntity(
                    tipoEvento = "BUSQUEDA",
                    termino = termino,
                    region = region ?: "",
                    rubro = rubro ?: ""
                )
            )
        }
    }

    // Registra un clic cuando el cliente presiona contactar
    override fun registrarClicContacto(region: String, rubro: String) {
        scope.launch {
            adminDao.registrarEvento(
                MetricaEventoEntity(
                    tipoEvento = "CLIC_CONTACTO",
                    region = region,
                    rubro = rubro
                )
            )
        }
    }

    // Registra una consulta cuando sale el primer mensaje
    override fun registrarConsultaEnviada(region: String, rubro: String) {
        scope.launch {
            adminDao.registrarEvento(
                MetricaEventoEntity(
                    tipoEvento = "CONSULTA_ENVIADA",
                    region = region,
                    rubro = rubro
                )
            )
        }
    }

    // Crea o edita una ficha administrativa en ms-usuarios y actualiza Room con la respuesta del servidor
    override suspend fun guardarFicha(ficha: FichaAdminEmprendimiento): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (token.isNullOrBlank()) {
            return@withContext Resultado.Error("Se requiere una sesión de administradora activa con token")
        }

        try {
            val emp = ficha.emprendedora
            val respuesta = if (emp.id <= 0) {
                usuariosApi.crearEmprendedoraAdmin(
                    autorizacion = "Bearer $token",
                    solicitud = RegistroEmprendedoraRequestDto(
                        nombre = emp.nombre.trim(),
                        email = emp.email.trim().lowercase(),
                        password = "emprendedora123",
                        aceptaLeyDatos = true,
                        rut = emp.rut.trim(),
                        nombreComercial = emp.nombreComercial.trim(),
                        region = emp.region.trim(),
                        comuna = emp.comuna.trim(),
                        rubros = emp.rubros.map { it.name },
                        instagram = emp.instagram?.trim()?.ifBlank { null }
                    )
                )
            } else {
                usuariosApi.editarEmprendedoraAdmin(
                    autorizacion = "Bearer $token",
                    id = emp.id.toLong(),
                    solicitud = EditarFichaAdminRequestDto(
                        nombreComercial = emp.nombreComercial.trim(),
                        region = emp.region.trim(),
                        comuna = emp.comuna.trim(),
                        rubros = emp.rubros.map { it.name },
                        instagram = emp.instagram?.trim()?.ifBlank { null }
                    )
                )
            }

            if (respuesta.isSuccessful && respuesta.body() != null) {
                val dto = respuesta.body()!!
                val empGuardada = dto.aModelo()
                val entidad = EmprendedoraEntity.desdeModelo(
                    emprendedora = empGuardada,
                    estadoFicha = dto.aEstadoFicha(),
                    problemas = AuditorFichas.auditar(empGuardada),
                    motivoRevision = dto.motivoRevision
                )
                emprendedoraDao.insertar(entidad)
                sincronizarFichas()
                Resultado.Exito(Unit)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(errorMsg)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resultado.Error("Error al guardar ficha en el servidor: ${e.message}")
        }
    }

    // Cambia el estado de moderación (APROBAR, OCULTAR, RECHAZAR) vía PATCH en ms-usuarios y actualiza Room
    override suspend fun cambiarEstadoFicha(
        emprendedoraId: Int,
        nuevoEstado: EstadoFichaEmprendimiento
    ): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (token.isNullOrBlank()) {
            return@withContext Resultado.Error("Se requiere una sesión de administradora activa con token")
        }

        val accionRemota = when (nuevoEstado) {
            EstadoFichaEmprendimiento.ACTIVA -> "APROBAR"
            EstadoFichaEmprendimiento.OCULTA -> "OCULTAR"
            EstadoFichaEmprendimiento.RECHAZADA -> "RECHAZAR"
            EstadoFichaEmprendimiento.DADA_DE_BAJA -> "OCULTAR"
            EstadoFichaEmprendimiento.POR_REVISAR ->
                return@withContext Resultado.Error("No es posible devolver una ficha a POR_REVISAR mediante moderación")
        }

        val motivo = if (accionRemota == "RECHAZAR") {
            "No cumple con los criterios de la Red Textil Circular"
        } else null

        try {
            val respuesta = usuariosApi.revisarEmprendedora(
                autorizacion = "Bearer $token",
                id = emprendedoraId.toLong(),
                solicitud = RevisionFichaRequestDto(accion = accionRemota, motivo = motivo)
            )
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val respDto = respuesta.body()!!
                val estadoConfirmado = try {
                    EstadoFichaEmprendimiento.valueOf(respDto.estadoFicha)
                } catch (_: Exception) {
                    nuevoEstado
                }
                val activa = estadoConfirmado == EstadoFichaEmprendimiento.ACTIVA
                emprendedoraDao.cambiarEstadoFicha(emprendedoraId, estadoConfirmado, activa)
                sincronizarFichas()
                Resultado.Exito(Unit)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(errorMsg)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resultado.Error("Error al moderar la ficha en el servidor: ${e.message}")
        }
    }

    // Elimina lógicamente una ficha en ms-usuarios y la remueve de la caché local
    override suspend fun eliminarFicha(emprendedoraId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = ClienteRetrofit.tokenActivo
        if (token.isNullOrBlank()) {
            return@withContext Resultado.Error("Se requiere una sesión de administradora activa con token")
        }
        try {
            val respuesta = usuariosApi.eliminarEmprendedoraAdmin(
                autorizacion = "Bearer $token",
                id = emprendedoraId.toLong()
            )
            if (respuesta.isSuccessful) {
                emprendedoraDao.eliminarPorId(emprendedoraId)
                sincronizarFichas()
                Resultado.Exito(Unit)
            } else {
                val errorMsg = ClienteRetrofit.extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(errorMsg)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resultado.Error("Error al eliminar la ficha en el servidor: ${e.message}")
        }
    }

    // Reintenta el envío de un correo fallido marcándolo como enviado
    override suspend fun reenviarCorreo(correoId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            adminDao.actualizarEstadoCorreo(correoId, EstadoCorreo.ENVIADO, "Reenviado hace un momento")
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("Error al reenviar el correo: ${e.message}")
        }
    }
}
