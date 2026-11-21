package com.example.textilcircular.ui.screens.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.ClienteRepository
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.ValidadorTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de UI para la pantalla de perfil del cliente.
data class MiPerfilClienteUiState(
    val nombre: String = "",
    val email: String = "",
    val nuevoNombre: String = "",
    val dialogandoEdicion: Boolean = false,
    val errorNombre: String? = null,
    val mensajeExito: String? = null,
    val estaGuardando: Boolean = false
)

// ViewModel para consultar y editar los datos personales del cliente, y cerrar sesión.
class MiPerfilClienteViewModel(
    private val sesionRepository: SesionRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiPerfilClienteUiState())
    val uiState: StateFlow<MiPerfilClienteUiState> = _uiState.asStateFlow()

    init {
        // Observamos la sesión activa para mantener los datos del cliente actualizados
        viewModelScope.launch {
            sesionRepository.estadoSesion.collect { estado ->
                if (estado is EstadoSesion.AutenticadoCliente) {
                    val cli = estado.cliente
                    _uiState.update {
                        it.copy(
                            nombre = cli.nombre,
                            email = cli.email,
                            nuevoNombre = cli.nombre
                        )
                    }
                }
            }
        }
    }

    fun abrirDialogoEdicion() {
        _uiState.update {
            it.copy(
                dialogandoEdicion = true,
                nuevoNombre = it.nombre,
                errorNombre = null
            )
        }
    }

    fun cerrarDialogoEdicion() {
        _uiState.update {
            it.copy(
                dialogandoEdicion = false,
                nuevoNombre = it.nombre,
                errorNombre = null
            )
        }
    }

    fun onNuevoNombreCambiado(valor: String) {
        _uiState.update {
            it.copy(
                nuevoNombre = valor,
                errorNombre = null
            )
        }
    }

    fun guardarNombre(onExito: (() -> Unit)? = null) {
        val actual = _uiState.value
        val resVacio = ValidadorTexto.validarNoVacio(actual.nuevoNombre, "El nombre")
        if (!resVacio.esValido) {
            _uiState.update { it.copy(errorNombre = resVacio.mensajeError) }
            return
        }

        val resLongitud = ValidadorTexto.validarLongitud(
            valor = actual.nuevoNombre,
            minimo = 3,
            maximo = 60,
            nombreCampo = "El nombre"
        )
        if (!resLongitud.esValido) {
            _uiState.update { it.copy(errorNombre = resLongitud.mensajeError) }
            return
        }

        val estadoSesionActual = sesionRepository.estadoSesion.value
        if (estadoSesionActual !is EstadoSesion.AutenticadoCliente) return

        val clienteActualizado = estadoSesionActual.cliente.copy(
            nombre = actual.nuevoNombre.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(estaGuardando = true) }
            val resultado = clienteRepository.actualizar(clienteActualizado)
            when (resultado) {
                is Resultado.Exito -> {
                    sesionRepository.actualizarClienteSesion(clienteActualizado)
                    _uiState.update {
                        it.copy(
                            nombre = clienteActualizado.nombre,
                            dialogandoEdicion = false,
                            estaGuardando = false,
                            mensajeExito = "Nombre actualizado correctamente"
                        )
                    }
                    onExito?.invoke()
                }
                is Resultado.Error -> {
                    _uiState.update {
                        it.copy(
                            errorNombre = resultado.mensaje,
                            estaGuardando = false
                        )
                    }
                }
            }
        }
    }

    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    fun cerrarSesion(onExito: () -> Unit) {
        viewModelScope.launch {
            sesionRepository.cerrarSesion()
            onExito()
        }
    }
}
