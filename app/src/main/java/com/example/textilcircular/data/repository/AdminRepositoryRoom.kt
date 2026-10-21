package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.AdminDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.RegistroAutomatizacionCorreo
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.AuditorFichas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Implementación del repositorio de administración respaldado en las tablas de SQLite.
class AdminRepositoryRoom(
    private val adminDao: AdminDao,
    private val emprendedoraDao: EmprendedoraDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AdminRepository {

    // Retorna todas las fichas para moderación administrativa
    override fun obtenerFichas(): Flow<List<FichaAdminEmprendimiento>> {
        return adminDao.obtenerFichas().map { lista ->
            lista.map { it.aFichaAdmin() }
        }
    }

    // Retorna una ficha específica con su diagnóstico de problemas
    override fun obtenerFichaPorId(id: Int): Flow<FichaAdminEmprendimiento?> {
        return adminDao.obtenerFichaPorId(id).map { it?.aFichaAdmin() }
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

    // Guarda una ficha auditando sus datos para detectar inconsistencias
    override suspend fun guardarFicha(ficha: FichaAdminEmprendimiento): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val problemas = AuditorFichas.auditar(ficha.emprendedora)
            val entidad = EmprendedoraEntity.desdeModelo(
                emprendedora = ficha.emprendedora,
                estadoFicha = ficha.estadoFicha,
                problemas = problemas
            )
            emprendedoraDao.insertar(entidad)

            // TODO: cuando haya servidor, sincronizar ficha administrativa con la API REST
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("Error al guardar la ficha: ${e.message}")
        }
    }

    // Cambia el estado de moderación (ACTIVA, OCULTA, POR_REVISAR, DADA_DE_BAJA)
    override suspend fun cambiarEstadoFicha(
        emprendedoraId: Int,
        nuevoEstado: EstadoFichaEmprendimiento
    ): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val activa = nuevoEstado == EstadoFichaEmprendimiento.ACTIVA
            val filas = emprendedoraDao.cambiarEstadoFicha(emprendedoraId, nuevoEstado, activa)
            if (filas > 0) {
                // TODO: cuando haya servidor, notificar cambio de estado de moderación a la API REST
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("Ficha de emprendimiento no encontrada")
            }
        } catch (e: Exception) {
            Resultado.Error("No se pudo cambiar el estado de la ficha: ${e.message}")
        }
    }

    // Elimina una ficha por completo de la base de datos
    override suspend fun eliminarFicha(emprendedoraId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val filas = emprendedoraDao.eliminarPorId(emprendedoraId)
            if (filas > 0) {
                // TODO: cuando haya servidor, registrar baja de ficha en la API REST
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("No se encontró la ficha a eliminar")
            }
        } catch (e: Exception) {
            Resultado.Error("Error al eliminar la ficha: ${e.message}")
        }
    }

    // Reintenta el envío de un correo fallido marcándolo como enviado
    override suspend fun reenviarCorreo(correoId: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            adminDao.actualizarEstadoCorreo(correoId, EstadoCorreo.ENVIADO, "Reenviado hace un momento")
            // TODO: cuando haya servidor, conectar con el servidor para reintento de envío de correo
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("Error al reenviar el correo: ${e.message}")
        }
    }
}
