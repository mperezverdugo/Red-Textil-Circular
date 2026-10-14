package com.example.textilcircular.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.util.Constantes
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.ValidadorContacto
import com.example.textilcircular.util.validacion.ValidadorContrasena
import com.example.textilcircular.util.validacion.ValidadorRut
import com.example.textilcircular.util.validacion.ValidadorTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario de registro guiado en 4 pasos.
data class RegistroGuiadoUiState(
    val pasoActual: Int = 1,

    // Paso 1: Datos personales y clave
    val nombreCompleto: String = "",
    val rut: String = "",
    val correo: String = "",
    val password: String = "",

    // Paso 2: Emprendimiento
    val nombreComercial: String = "",
    val rubrosSeleccionados: List<Rubro> = emptyList(),

    // Paso 3: Ubicación y contacto
    val regionSeleccionada: String = "",
    val comunaSeleccionada: String = "",
    val instagram: String = "",

    // Paso 4: Confirmación y condiciones
    val aceptaLeyDatos: Boolean = false,

    // Mensajes de error por campo
    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorNombreComercial: String? = null,
    val errorRubros: String? = null,
    val errorRegion: String? = null,
    val errorComuna: String? = null,
    val errorInstagram: String? = null,
    val errorGeneral: String? = null,

    val estaCargando: Boolean = false
)

// ViewModel que administra el avance, las validaciones paso a paso y la creación de la cuenta.
class RegistroGuiadoViewModel(
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroGuiadoUiState())
    val uiState: StateFlow<RegistroGuiadoUiState> = _uiState.asStateFlow()

    fun onNombreCompletoCambiado(valor: String) {
        _uiState.update { it.copy(nombreCompleto = valor, errorNombre = null) }
    }

    fun onRutCambiado(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onCorreoCambiado(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun onPasswordCambiado(valor: String) {
        _uiState.update { it.copy(password = valor, errorPassword = null) }
    }

    fun onNombreComercialCambiado(valor: String) {
        _uiState.update { it.copy(nombreComercial = valor, errorNombreComercial = null) }
    }

    fun onRubroToggled(rubro: Rubro) {
        _uiState.update { actual ->
            val lista = actual.rubrosSeleccionados.toMutableList()
            if (lista.contains(rubro)) {
                lista.remove(rubro)
                actual.copy(rubrosSeleccionados = lista, errorRubros = null)
            } else {
                if (lista.size < Constantes.MAX_RUBROS) {
                    lista.add(rubro)
                    actual.copy(rubrosSeleccionados = lista, errorRubros = null)
                } else {
                    actual.copy(errorRubros = "Solo puedes elegir hasta ${Constantes.MAX_RUBROS} rubros")
                }
            }
        }
    }

    fun onRegionSeleccionada(region: String) {
        _uiState.update {
            it.copy(
                regionSeleccionada = region,
                comunaSeleccionada = "",
                errorRegion = null,
                errorComuna = null
            )
        }
    }

    fun onComunaSeleccionada(comuna: String) {
        _uiState.update { it.copy(comunaSeleccionada = comuna, errorComuna = null) }
    }

    fun onInstagramCambiado(valor: String) {
        _uiState.update { it.copy(instagram = valor, errorInstagram = null) }
    }

    fun onAceptaLeyDatosCambiado(acepta: Boolean) {
        _uiState.update { it.copy(aceptaLeyDatos = acepta, errorGeneral = null) }
    }

    fun avanzarPaso(): Boolean {
        val actual = _uiState.value

        when (actual.pasoActual) {
            1 -> {
                val resNombre = ValidadorTexto.validarNoVacio(actual.nombreCompleto, "El nombre")
                val resRut = ValidadorRut.validar(actual.rut)
                val resCorreo = ValidadorContacto.validarEmail(actual.correo)
                val resClave = ValidadorContrasena.validar(actual.password)

                val hayError = !resNombre.esValido || !resRut.esValido || !resCorreo.esValido || !resClave.esValido
                if (hayError) {
                    _uiState.update {
                        it.copy(
                            errorNombre = resNombre.mensajeError,
                            errorRut = resRut.mensajeError,
                            errorCorreo = resCorreo.mensajeError,
                            errorPassword = resClave.mensajeError
                        )
                    }
                    return false
                }

                // Formatear RUT y pasar a paso 2
                val rutFormateado = ValidadorRut.formatear(actual.rut)
                _uiState.update {
                    it.copy(
                        rut = rutFormateado,
                        pasoActual = 2,
                        errorNombre = null,
                        errorRut = null,
                        errorCorreo = null,
                        errorPassword = null
                    )
                }
                return true
            }

            2 -> {
                val resNombreComercial = ValidadorTexto.validarNoVacio(actual.nombreComercial, "El nombre comercial")
                val rubrosValidos = actual.rubrosSeleccionados.isNotEmpty()

                if (!resNombreComercial.esValido || !rubrosValidos) {
                    _uiState.update {
                        it.copy(
                            errorNombreComercial = resNombreComercial.mensajeError,
                            errorRubros = if (!rubrosValidos) "Debes seleccionar al menos un rubro" else null
                        )
                    }
                    return false
                }

                _uiState.update {
                    it.copy(
                        pasoActual = 3,
                        errorNombreComercial = null,
                        errorRubros = null
                    )
                }
                return true
            }

            3 -> {
                val resRegion = ValidadorTexto.validarNoVacio(actual.regionSeleccionada, "La región")
                val resComuna = ValidadorTexto.validarNoVacio(actual.comunaSeleccionada, "La comuna")
                val resIg = ValidadorContacto.validarInstagram(actual.instagram)

                val hayError = !resRegion.esValido || !resComuna.esValido || !resIg.esValido
                if (hayError) {
                    _uiState.update {
                        it.copy(
                            errorRegion = resRegion.mensajeError,
                            errorComuna = resComuna.mensajeError,
                            errorInstagram = resIg.mensajeError
                        )
                    }
                    return false
                }

                _uiState.update {
                    it.copy(
                        pasoActual = 4,
                        errorRegion = null,
                        errorComuna = null,
                        errorInstagram = null
                    )
                }
                return true
            }

            else -> return true
        }
    }

    fun retrocederPaso(): Boolean {
        val actual = _uiState.value
        return if (actual.pasoActual > 1) {
            _uiState.update { it.copy(pasoActual = it.pasoActual - 1) }
            true
        } else {
            false
        }
    }

    fun finalizarRegistro(onExito: () -> Unit) {
        val actual = _uiState.value
        if (!actual.aceptaLeyDatos) {
            _uiState.update { it.copy(errorGeneral = "Debes aceptar los términos y el consentimiento de datos") }
            return
        }

        _uiState.update { it.copy(estaCargando = true, errorGeneral = null) }

        viewModelScope.launch {
            val nuevaEmprendedora = Emprendedora(
                id = 0,
                rut = actual.rut,
                nombre = actual.nombreCompleto.trim(),
                nombreComercial = actual.nombreComercial.trim(),
                region = actual.regionSeleccionada,
                comuna = actual.comunaSeleccionada,
                rubros = actual.rubrosSeleccionados,
                email = actual.correo.trim().lowercase(),
                instagram = actual.instagram.trim().ifBlank { null }?.let { ValidadorContacto.limpiarUsuarioInstagram(it) },
                activa = true
            )

            when (val res = sesionRepository.registrarEmprendedora(nuevaEmprendedora, actual.password)) {
                is Resultado.Exito -> {
                    _uiState.update { it.copy(estaCargando = false) }
                    onExito()
                }
                is Resultado.Error -> {
                    _uiState.update {
                        it.copy(
                            estaCargando = false,
                            errorGeneral = res.mensaje
                        )
                    }
                }
            }
        }
    }
}
