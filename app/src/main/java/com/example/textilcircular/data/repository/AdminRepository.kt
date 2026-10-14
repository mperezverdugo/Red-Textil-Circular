package com.example.textilcircular.data.repository

import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.RegistroAutomatizacionCorreo
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.AuditorFichas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Métricas agregadas de impacto y trazabilidad para el panel de administración.
data class MetricasAdmin(
    val totalBusquedas: Int,
    val totalConsultas: Int,
    val totalClics: Int,
    val fichasPorRevisar: Int,
    val contactosPorRegion: List<Pair<String, Int>>,
    val contactosPorRubro: List<Pair<String, Int>>
)

// Contrato para la administración de fichas, métricas de impacto y automatizaciones.
interface AdminRepository {
    fun obtenerFichas(): Flow<List<FichaAdminEmprendimiento>>
    fun obtenerFichaPorId(id: Int): Flow<FichaAdminEmprendimiento?>
    fun obtenerMetricas(): Flow<MetricasAdmin>
    fun obtenerCorreos(): Flow<List<RegistroAutomatizacionCorreo>>
    suspend fun guardarFicha(ficha: FichaAdminEmprendimiento): Resultado<Unit>
    suspend fun cambiarEstadoFicha(emprendedoraId: Int, nuevoEstado: EstadoFichaEmprendimiento): Resultado<Unit>
    suspend fun eliminarFicha(emprendedoraId: Int): Resultado<Unit>
    suspend fun reenviarCorreo(correoId: Int): Resultado<Unit>
}

// Implementación en memoria para la gestión y moderación administrativa.
class AdminRepositoryMemoria(
    private val emprendedoraRepository: EmprendedoraRepository
) : AdminRepository {

    private val fichas = DatosDemo.EmprendimientosAdminDemo.toMutableList()
    private val correos = DatosDemo.AutomatizacionesCorreosDemo.toMutableList()

    private val _fichasFlow = MutableStateFlow<List<FichaAdminEmprendimiento>>(fichas.toList())
    private val _correosFlow = MutableStateFlow<List<RegistroAutomatizacionCorreo>>(correos.toList())

    override fun obtenerFichas(): Flow<List<FichaAdminEmprendimiento>> = _fichasFlow.asStateFlow()

    override fun obtenerFichaPorId(id: Int): Flow<FichaAdminEmprendimiento?> {
        return _fichasFlow.map { list -> list.firstOrNull { it.emprendedora.id == id } }
    }

    override fun obtenerMetricas(): Flow<MetricasAdmin> {
        val fichasRevisar = fichas.count { it.estadoFicha == EstadoFichaEmprendimiento.POR_REVISAR }
        val metricas = MetricasAdmin(
            totalBusquedas = DatosDemo.TotalBusquedasMes,
            totalConsultas = DatosDemo.TotalConsultasEnviadas,
            totalClics = DatosDemo.TotalClicsContacto,
            fichasPorRevisar = if (fichasRevisar > 0) fichasRevisar else DatosDemo.FichasPorRevisarContador,
            contactosPorRegion = DatosDemo.ContactosPorRegionDemo,
            contactosPorRubro = DatosDemo.ContactosPorRubroDemo
        )
        return MutableStateFlow(metricas).asStateFlow()
    }

    override fun obtenerCorreos(): Flow<List<RegistroAutomatizacionCorreo>> = _correosFlow.asStateFlow()

    override suspend fun guardarFicha(ficha: FichaAdminEmprendimiento): Resultado<Unit> {
        val problemas = AuditorFichas.auditar(ficha.emprendedora)
        val fichaAuditada = ficha.copy(problemasDetectados = problemas)

        val idx = fichas.indexOfFirst { it.emprendedora.id == ficha.emprendedora.id }
        if (idx >= 0) {
            fichas[idx] = fichaAuditada
        } else {
            fichas.add(fichaAuditada)
        }
        _fichasFlow.value = fichas.toList()

        // Sincronizar también con el repositorio de emprendedoras
        emprendedoraRepository.guardar(ficha.emprendedora)
        return Resultado.Exito(Unit)
    }

    override suspend fun cambiarEstadoFicha(
        emprendedoraId: Int,
        nuevoEstado: EstadoFichaEmprendimiento
    ): Resultado<Unit> {
        val idx = fichas.indexOfFirst { it.emprendedora.id == emprendedoraId }
        return if (idx >= 0) {
            val fichaActual = fichas[idx]
            val activa = nuevoEstado == EstadoFichaEmprendimiento.ACTIVA
            fichas[idx] = fichaActual.copy(
                estadoFicha = nuevoEstado,
                emprendedora = fichaActual.emprendedora.copy(activa = activa)
            )
            _fichasFlow.value = fichas.toList()
            emprendedoraRepository.cambiarVisibilidad(emprendedoraId, activa)
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("Ficha de emprendimiento no encontrada")
        }
    }

    override suspend fun eliminarFicha(emprendedoraId: Int): Resultado<Unit> {
        val eliminada = fichas.removeAll { it.emprendedora.id == emprendedoraId }
        return if (eliminada) {
            _fichasFlow.value = fichas.toList()
            emprendedoraRepository.darDeBaja(emprendedoraId)
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("No se encontró la ficha a eliminar")
        }
    }

    override suspend fun reenviarCorreo(correoId: Int): Resultado<Unit> {
        val idx = correos.indexOfFirst { it.id == correoId }
        return if (idx >= 0) {
            correos[idx] = correos[idx].copy(
                estado = EstadoCorreo.ENVIADO,
                fechaEnvio = "Reenviado hace un momento"
            )
            _correosFlow.value = correos.toList()
            // TODO: conectar con el servidor para reintento de envío de correo
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("Registro de correo no encontrado")
        }
    }
}
