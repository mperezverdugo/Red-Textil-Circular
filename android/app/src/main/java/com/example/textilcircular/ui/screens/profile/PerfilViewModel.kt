package com.example.textilcircular.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.EmprendedoraRepository
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.data.repository.SincronizacionRepository
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.ItemSincronizacion
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Constantes
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.ValidadorContacto
import com.example.textilcircular.util.validacion.ValidadorTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de UI para la gestión del perfil, edición de datos, sincronización y baja.
data class PerfilUiState(
    val emprendedora: Emprendedora = DatosDemo.EmprendedoraDemo,
    val modoSinConexion: Boolean = false,
    val simularListasVacias: Boolean = false,
    val estaCargando: Boolean = false,

    // Campos de edición de perfil
    val editNombre: String = "",
    val editNombreComercial: String = "",
    val editRegion: String = "",
    val editComuna: String = "",
    val editRubros: List<Rubro> = emptyList(),
    val editInstagram: String = "",

    // Errores de validación de edición
    val errorNombre: String? = null,
    val errorNombreComercial: String? = null,
    val errorRegion: String? = null,
    val errorComuna: String? = null,
    val errorRubros: String? = null,
    val errorInstagram: String? = null,
    val errorGeneral: String? = null,

    // Sincronización
    val itemsPendientes: List<ItemSincronizacion> = emptyList(),
    val estadoSincronizacion: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE
)

// ViewModel que administra la información personal, su edición, sincronización y ciclo de vida de la cuenta.
class PerfilViewModel(
    private val sesionRepository: SesionRepository,
    private val emprendedoraRepository: EmprendedoraRepository,
    private val sincronizacionRepository: SincronizacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        // Observar la sesión activa para mantener los datos de la emprendedora sincronizados
        viewModelScope.launch {
            sesionRepository.estadoSesion.collect { estado ->
                when (estado) {
                    is EstadoSesion.AutenticadoEmprendedora -> {
                        val emp = estado.emprendedora
                        _uiState.update {
                            it.copy(
                                emprendedora = emp,
                                editNombre = emp.nombre,
                                editNombreComercial = emp.nombreComercial,
                                editRegion = emp.region,
                                editComuna = emp.comuna,
                                editRubros = emp.rubros,
                                editInstagram = emp.instagram ?: ""
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        // Observar elementos pendientes de sincronización
        viewModelScope.launch {
            sincronizacionRepository.obtenerPendientes().collect { pendientes ->
                _uiState.update { it.copy(itemsPendientes = pendientes) }
            }
        }

        // Observar estado de la sincronización
        viewModelScope.launch {
            sincronizacionRepository.estadoSincronizacion.collect { estadoSync ->
                _uiState.update { it.copy(estadoSincronizacion = estadoSync) }
            }
        }

        // Observar modo sin conexión centralizado
        viewModelScope.launch {
            sincronizacionRepository.modoSinConexion.collect { offline ->
                _uiState.update { it.copy(modoSinConexion = offline) }
            }
        }
    }

    fun onModoSinConexionCambiado(activado: Boolean) {
        _uiState.update { it.copy(modoSinConexion = activado) }
        sincronizacionRepository.setModoSinConexion(activado)
    }

    fun onSimularListasVaciasCambiado(activado: Boolean) {
        _uiState.update { it.copy(simularListasVacias = activado) }
    }

    fun prepararEdicion() {
        val emp = _uiState.value.emprendedora
        _uiState.update {
            it.copy(
                editNombre = emp.nombre,
                editNombreComercial = emp.nombreComercial,
                editRegion = emp.region,
                editComuna = emp.comuna,
                editRubros = emp.rubros,
                editInstagram = emp.instagram ?: "",
                errorNombre = null,
                errorNombreComercial = null,
                errorRegion = null,
                errorComuna = null,
                errorRubros = null,
                errorInstagram = null,
                errorGeneral = null
            )
        }
    }

    fun onEditNombreCambiado(valor: String) {
        _uiState.update { it.copy(editNombre = valor, errorNombre = null) }
    }

    fun onEditNombreComercialCambiado(valor: String) {
        _uiState.update { it.copy(editNombreComercial = valor, errorNombreComercial = null) }
    }

    fun onEditRegionSeleccionada(region: String) {
        _uiState.update {
            it.copy(
                editRegion = region,
                editComuna = "",
                errorRegion = null,
                errorComuna = null
            )
        }
    }

    fun onEditComunaSeleccionada(comuna: String) {
        _uiState.update { it.copy(editComuna = comuna, errorComuna = null) }
    }

    fun onEditInstagramCambiado(valor: String) {
        _uiState.update { it.copy(editInstagram = valor, errorInstagram = null) }
    }

    fun onEditRubroToggled(rubro: Rubro) {
        _uiState.update { actual ->
            val lista = actual.editRubros.toMutableList()
            if (lista.contains(rubro)) {
                lista.remove(rubro)
                actual.copy(editRubros = lista, errorRubros = null)
            } else {
                if (lista.size < Constantes.MAX_RUBROS) {
                    lista.add(rubro)
                    actual.copy(editRubros = lista, errorRubros = null)
                } else {
                    actual.copy(errorRubros = "Solo puedes elegir hasta ${Constantes.MAX_RUBROS} rubros")
                }
            }
        }
    }

    fun guardarCambiosPerfil(onExito: () -> Unit) {
        val actual = _uiState.value

        val resNombre = ValidadorTexto.validarNoVacio(actual.editNombre, "El nombre")
        val resNombreComercial = ValidadorTexto.validarNoVacio(actual.editNombreComercial, "El nombre comercial")
        val resRegion = ValidadorTexto.validarNoVacio(actual.editRegion, "La región")
        val resComuna = ValidadorTexto.validarNoVacio(actual.editComuna, "La comuna")
        val rubrosValidos = actual.editRubros.isNotEmpty()
        val resIg = ValidadorContacto.validarInstagram(actual.editInstagram)

        val hayError = !resNombre.esValido || !resNombreComercial.esValido ||
                !resRegion.esValido || !resComuna.esValido || !rubrosValidos || !resIg.esValido

        if (hayError) {
            _uiState.update {
                it.copy(
                    errorNombre = resNombre.mensajeError,
                    errorNombreComercial = resNombreComercial.mensajeError,
                    errorRegion = resRegion.mensajeError,
                    errorComuna = resComuna.mensajeError,
                    errorRubros = if (!rubrosValidos) "Debes seleccionar al menos un rubro" else null,
                    errorInstagram = resIg.mensajeError
                )
            }
            return
        }

        val emprendedoraActualizada = actual.emprendedora.copy(
            nombre = actual.editNombre.trim(),
            nombreComercial = actual.editNombreComercial.trim(),
            region = actual.editRegion,
            comuna = actual.editComuna,
            rubros = actual.editRubros,
            instagram = actual.editInstagram.trim().ifBlank { null }?.let { ValidadorContacto.limpiarUsuarioInstagram(it) }
        )

        viewModelScope.launch {
            emprendedoraRepository.actualizar(emprendedoraActualizada)
            sesionRepository.actualizarEmprendedoraSesion(emprendedoraActualizada)
            val esOffline = sincronizacionRepository.modoSinConexion.value
            sincronizacionRepository.registrarPendiente(
                tipo = "Actualización de perfil",
                detalle = if (esOffline) {
                    "Cambio offline: Datos comerciales de ${emprendedoraActualizada.nombreComercial}"
                } else {
                    "Cambio de datos comerciales de ${emprendedoraActualizada.nombreComercial}"
                }
            )
            onExito()
        }
    }

    fun forzarSincronizacion() {
        viewModelScope.launch {
            sincronizacionRepository.sincronizar()
        }
    }

    fun solicitarBajaVoluntaria(onExito: () -> Unit) {
        val emp = _uiState.value.emprendedora
        viewModelScope.launch {
            emprendedoraRepository.darDeBaja(emp.id)
            sesionRepository.cerrarSesion()
            onExito()
        }
    }

    fun cerrarSesion(onExito: () -> Unit) {
        viewModelScope.launch {
            sesionRepository.cerrarSesion()
            onExito()
        }
    }
}
