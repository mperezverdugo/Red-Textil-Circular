package com.example.textilcircular.ui.screens.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.ValidadorContacto
import com.example.textilcircular.util.validacion.ValidadorContrasena
import com.example.textilcircular.util.validacion.ValidadorTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla para el formulario de registro de un cliente.
data class RegistroClienteUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val aceptaLeyDatos: Boolean = false,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val estaCargando: Boolean = false
)

// ViewModel encargado de validar y crear la cuenta del cliente en la aplicación.
class RegistroClienteViewModel(
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroClienteUiState())
    val uiState: StateFlow<RegistroClienteUiState> = _uiState.asStateFlow()

    fun onNombreCambiado(nuevoNombre: String) {
        _uiState.update { it.copy(nombre = nuevoNombre, errorNombre = null, errorGeneral = null) }
    }

    fun onCorreoCambiado(nuevoCorreo: String) {
        _uiState.update { it.copy(correo = nuevoCorreo, errorCorreo = null, errorGeneral = null) }
    }

    fun onPasswordCambiado(nuevaPassword: String) {
        _uiState.update { it.copy(password = nuevaPassword, errorPassword = null, errorGeneral = null) }
    }

    fun onAceptaLeyDatosCambiado(acepta: Boolean) {
        _uiState.update { it.copy(aceptaLeyDatos = acepta, errorGeneral = null) }
    }

    fun registrar(onExito: () -> Unit) {
        val actual = _uiState.value

        val resNombre = ValidadorTexto.validarNoVacio(actual.nombre, "El nombre")
        val resCorreo = ValidadorContacto.validarEmail(actual.correo)
        val resClave = ValidadorContrasena.validar(actual.password)

        val hayErrorCampos = !resNombre.esValido || !resCorreo.esValido || !resClave.esValido
        if (hayErrorCampos) {
            _uiState.update {
                it.copy(
                    errorNombre = resNombre.mensajeError,
                    errorCorreo = resCorreo.mensajeError,
                    errorPassword = resClave.mensajeError
                )
            }
            return
        }

        if (!actual.aceptaLeyDatos) {
            _uiState.update {
                it.copy(errorGeneral = "Debes aceptar los términos para registrar tu cuenta.")
            }
            return
        }

        _uiState.update { it.copy(estaCargando = true, errorGeneral = null) }

        viewModelScope.launch {
            val resultado = sesionRepository.registrarCliente(
                nombre = actual.nombre.trim(),
                correo = actual.correo.trim(),
                clave = actual.password,
                aceptaDatos = actual.aceptaLeyDatos
            )

            when (resultado) {
                is Resultado.Exito -> {
                    _uiState.update { it.copy(estaCargando = false) }
                    onExito()
                }
                is Resultado.Error -> {
                    _uiState.update {
                        it.copy(
                            estaCargando = false,
                            errorGeneral = resultado.mensaje
                        )
                    }
                }
            }
        }
    }
}
