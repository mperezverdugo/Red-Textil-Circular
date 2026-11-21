package com.example.textilcircular.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.ValidadorContacto
import com.example.textilcircular.util.validacion.ValidadorContrasena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la interfaz de usuario para el inicio de sesión.
data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val estaCargando: Boolean = false
)

// ViewModel encargado de procesar las credenciales y el acceso a la sesión según el rol.
class LoginViewModel(
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambiado(nuevoCorreo: String) {
        _uiState.update {
            it.copy(
                correo = nuevoCorreo,
                errorCorreo = null,
                errorGeneral = null
            )
        }
    }

    fun onPasswordCambiado(nuevaPassword: String) {
        _uiState.update {
            it.copy(
                password = nuevaPassword,
                errorPassword = null,
                errorGeneral = null
            )
        }
    }

    fun iniciarSesion(
        onExitoEmprendedora: () -> Unit,
        onExitoAdmin: () -> Unit,
        onExitoCliente: () -> Unit = {}
    ) {
        val estadoActual = _uiState.value
        val resEmail = ValidadorContacto.validarEmail(estadoActual.correo)
        val resClave = ValidadorContrasena.validar(estadoActual.password)

        if (!resEmail.esValido || !resClave.esValido) {
            _uiState.update {
                it.copy(
                    errorCorreo = resEmail.mensajeError,
                    errorPassword = resClave.mensajeError
                )
            }
            return
        }

        _uiState.update { it.copy(estaCargando = true, errorGeneral = null) }

        viewModelScope.launch {
            val correoLimpio = estadoActual.correo.trim().lowercase()
            val esCuentaAdmin = correoLimpio.contains("admin")

            if (esCuentaAdmin) {
                when (val res = sesionRepository.iniciarSesionAdmin(correoLimpio, estadoActual.password)) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(estaCargando = false) }
                        onExitoAdmin()
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
            } else {
                // Primero comprobamos si es cuenta de cliente
                val resCliente = sesionRepository.iniciarSesionCliente(correoLimpio, estadoActual.password)
                if (resCliente is Resultado.Exito) {
                    _uiState.update { it.copy(estaCargando = false) }
                    onExitoCliente()
                } else {
                    // Si no es cliente, intentamos como emprendedora
                    when (val resEmp = sesionRepository.iniciarSesionEmprendedora(correoLimpio, estadoActual.password)) {
                        is Resultado.Exito -> {
                            _uiState.update { it.copy(estaCargando = false) }
                            onExitoEmprendedora()
                        }
                        is Resultado.Error -> {
                            _uiState.update {
                                it.copy(
                                    estaCargando = false,
                                    errorGeneral = resEmp.mensaje
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun iniciarSesionDemoEmprendedora(onExito: () -> Unit) {
        // TODO: quitar esta opción de demostración al terminar el proyecto
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            sesionRepository.iniciarSesionDemoEmprendedora()
            _uiState.update { it.copy(estaCargando = false) }
            onExito()
        }
    }

    fun iniciarSesionDemoCliente(onExito: () -> Unit) {
        // TODO: quitar esta opción de demostración al terminar el proyecto
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            sesionRepository.iniciarSesionDemoCliente()
            _uiState.update { it.copy(estaCargando = false) }
            onExito()
        }
    }

    fun iniciarSesionDemoAdmin(onExito: () -> Unit) {
        // TODO: quitar esta opción de demostración al terminar el proyecto
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            sesionRepository.iniciarSesionDemoAdmin()
            _uiState.update { it.copy(estaCargando = false) }
            onExito()
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(errorGeneral = null) }
    }
}
