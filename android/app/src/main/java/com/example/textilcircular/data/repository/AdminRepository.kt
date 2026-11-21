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
    fun registrarBusqueda(termino: String, region: String? = null, rubro: String? = null)
    fun registrarClicContacto(region: String, rubro: String)
    fun registrarConsultaEnviada(region: String, rubro: String)
}

// Implementación en memoria para la gestión y moderación administrativa.
class AdminRepositoryMemoria(
    private val emprendedoraRepository: EmprendedoraRepository
) : AdminRepository {

    private val fichas = DatosDemo.EmprendimientosAdminDemo.toMutableList()
    private val correos = DatosDemo.AutomatizacionesCorreosDemo.toMutableList()

    private var totalBusquedas = DatosDemo.TotalBusquedasMes
    private var totalConsultas = DatosDemo.TotalConsultasEnviadas
    private var totalClics = DatosDemo.TotalClicsContacto
    private val contactosPorRegion = DatosDemo.ContactosPorRegionDemo.toMap().toMutableMap()
    private val contactosPorRubro = DatosDemo.ContactosPorRubroDemo.toMap().toMutableMap()

    private val _fichasFlow = MutableStateFlow<List<FichaAdminEmprendimiento>>(fichas.toList())
    private val _correosFlow = MutableStateFlow<List<RegistroAutomatizacionCorreo>>(correos.toList())
    private val _metricasFlow = MutableStateFlow(calcularMetricas())

    // Recalcula los números agregados para el panel de administración
    private fun calcularMetricas(): MetricasAdmin {
        val fichasRevisar = fichas.count { it.estadoFicha == EstadoFichaEmprendimiento.POR_REVISAR }
        return MetricasAdmin(
            totalBusquedas = totalBusquedas,
            totalConsultas = totalConsultas,
            totalClics = totalClics,
            fichasPorRevisar = if (fichasRevisar > 0) fichasRevisar else DatosDemo.FichasPorRevisarContador,
            contactosPorRegion = contactosPorRegion.toList(),
            contactosPorRubro = contactosPorRubro.toList()
        )
    }

    override fun obtenerFichas(): Flow<List<FichaAdminEmprendimiento>> = _fichasFlow.asStateFlow()

    override fun obtenerFichaPorId(id: Int): Flow<FichaAdminEmprendimiento?> {
        return _fichasFlow.map { list -> list.firstOrNull { it.emprendedora.id == id } }
    }

    override fun obtenerMetricas(): Flow<MetricasAdmin> = _metricasFlow.asStateFlow()

    override fun obtenerCorreos(): Flow<List<RegistroAutomatizacionCorreo>> = _correosFlow.asStateFlow()

    // Registra una búsqueda cuando el cliente busca o cambia un filtro
    override fun registrarBusqueda(termino: String, region: String?, rubro: String?) {
        totalBusquedas++
        _metricasFlow.value = calcularMetricas()
    }

    // Registra un clic cuando el cliente decide contactar
    override fun registrarClicContacto(region: String, rubro: String) {
        totalClics++
        if (region.isNotBlank()) {
            contactosPorRegion[region] = (contactosPorRegion[region] ?: 0) + 1
        }
        if (rubro.isNotBlank()) {
            contactosPorRubro[rubro] = (contactosPorRubro[rubro] ?: 0) + 1
        }
        _metricasFlow.value = calcularMetricas()
    }

    // Registra una consulta enviada cuando sale el primer mensaje
    override fun registrarConsultaEnviada(region: String, rubro: String) {
        totalConsultas++
        if (region.isNotBlank()) {
            contactosPorRegion[region] = (contactosPorRegion[region] ?: 0) + 1
        }
        if (rubro.isNotBlank()) {
            contactosPorRubro[rubro] = (contactosPorRubro[rubro] ?: 0) + 1
        }
        _metricasFlow.value = calcularMetricas()
    }

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
        _metricasFlow.value = calcularMetricas()

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
            _metricasFlow.value = calcularMetricas()
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
            _metricasFlow.value = calcularMetricas()
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
