package com.example.textilcircular.ui.screens.catalog

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.ProductoRepository
import com.example.textilcircular.data.repository.SesionRepository
import com.example.textilcircular.model.Producto
import com.example.textilcircular.util.Constantes
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.media.CompresorImagen
import com.example.textilcircular.util.validacion.ValidadorProducto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario de creación y edición de productos.
data class ProductoFormUiState(
    val id: Int = 0,
    val nombre: String = "",
    val descripcionCorta: String = "",
    val explicacionProceso: String = "",
    val fotos: List<String> = emptyList(),

    // Estados de procesamiento y compresión
    val estaProcesandoFoto: Boolean = false,
    val mensajeEstadoFoto: String? = null,
    val estaGuardando: Boolean = false,

    // Errores de validación (auditoría de límites)
    val errorNombre: String? = null,
    val errorDescripcion: String? = null,
    val errorProceso: String? = null,
    val errorFotos: String? = null,
    val errorGeneral: String? = null,

    // Gestión de permisos de cámara
    val mostrarExplicacionPermisoCamara: Boolean = false,
    val mostrarDialogoIrAAjustes: Boolean = false
)

// ViewModel para administrar la carga, validación, captura y compresión de fotos de un producto.
class ProductoFormViewModel(
    private val productoRepository: ProductoRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoFormUiState())
    val uiState: StateFlow<ProductoFormUiState> = _uiState.asStateFlow()

    fun cargarProducto(productoId: Int) {
        if (productoId <= 0) return

        viewModelScope.launch {
            val producto = productoRepository.obtenerPorId(productoId).firstOrNull()
            if (producto != null) {
                _uiState.update {
                    it.copy(
                        id = producto.id,
                        nombre = producto.nombre,
                        descripcionCorta = producto.descripcionCorta,
                        explicacionProceso = producto.explicacionProceso ?: "",
                        fotos = producto.fotos
                    )
                }
            }
        }
    }

    fun onNombreCambiado(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun onDescripcionCambiado(valor: String) {
        _uiState.update { it.copy(descripcionCorta = valor, errorDescripcion = null) }
    }

    fun onExplicacionProcesoCambiado(valor: String) {
        _uiState.update { it.copy(explicacionProceso = valor, errorProceso = null) }
    }

    fun eliminarFoto(indice: Int) {
        _uiState.update { actual ->
            val lista = actual.fotos.toMutableList()
            if (indice in lista.indices) {
                lista.removeAt(indice)
            }
            actual.copy(fotos = lista, errorFotos = null)
        }
    }

    fun procesarFoto(context: Context, origenUri: Uri, esTemporal: Boolean = false) {
        val actual = _uiState.value

        // Auditoría de límites: no exceder el máximo de fotos permitido
        if (actual.fotos.size >= Constantes.MAX_FOTOS_POR_PRODUCTO) {
            _uiState.update {
                it.copy(errorFotos = "No puedes agregar más de ${Constantes.MAX_FOTOS_POR_PRODUCTO} fotos")
            }
            return
        }

        _uiState.update {
            it.copy(
                estaProcesandoFoto = true,
                mensajeEstadoFoto = "Preparando foto…",
                errorGeneral = null
            )
        }

        viewModelScope.launch {
            val resultado = CompresorImagen.procesarYGuardarFoto(
                context = context,
                origenUri = origenUri,
                borrarOrigenSiEsArchivo = esTemporal
            )

            when (resultado) {
                is Resultado.Exito -> {
                    _uiState.update {
                        it.copy(
                            fotos = it.fotos + resultado.datos,
                            estaProcesandoFoto = false,
                            mensajeEstadoFoto = null,
                            errorFotos = null
                        )
                    }
                }
                is Resultado.Error -> {
                    _uiState.update {
                        it.copy(
                            estaProcesandoFoto = false,
                            mensajeEstadoFoto = null,
                            errorGeneral = resultado.mensaje
                        )
                    }
                }
            }
        }
    }

    fun onPermisoCamaraDenegado(noVolverAPreguntar: Boolean) {
        if (noVolverAPreguntar) {
            _uiState.update { it.copy(mostrarDialogoIrAAjustes = true) }
        } else {
            _uiState.update { it.copy(mostrarExplicacionPermisoCamara = true) }
        }
    }

    fun cerrarDialogoExplicacionPermiso() {
        _uiState.update { it.copy(mostrarExplicacionPermisoCamara = false) }
    }

    fun cerrarDialogoAjustes() {
        _uiState.update { it.copy(mostrarDialogoIrAAjustes = false) }
    }

    fun guardarProducto(onExito: () -> Unit) {
        val actual = _uiState.value

        val resValidacion = ValidadorProducto.validarFormularioCompleto(
            nombre = actual.nombre,
            descripcion = actual.descripcionCorta,
            explicacionProceso = actual.explicacionProceso,
            fotos = actual.fotos,
            obligatoriaAlMenosUnaFoto = false
        )

        if (!resValidacion.esValido) {
            val resNombre = ValidadorProducto.validarNombre(actual.nombre)
            val resDesc = ValidadorProducto.validarDescripcion(actual.descripcionCorta)
            val resProceso = ValidadorProducto.validarExplicacionProceso(actual.explicacionProceso)
            val resFotos = ValidadorProducto.validarFotos(actual.fotos)

            _uiState.update {
                it.copy(
                    errorNombre = resNombre.mensajeError,
                    errorDescripcion = resDesc.mensajeError,
                    errorProceso = resProceso.mensajeError,
                    errorFotos = resFotos.mensajeError,
                    errorGeneral = resValidacion.mensajeError
                )
            }
            return
        }

        _uiState.update { it.copy(estaGuardando = true) }

        viewModelScope.launch {
            val estadoSesion = sesionRepository.estadoSesion.value
            val empId = when (estadoSesion) {
                is EstadoSesion.AutenticadoEmprendedora -> estadoSesion.emprendedora.id
                else -> 1
            }

            val producto = Producto(
                id = actual.id,
                emprendedoraId = empId,
                nombre = actual.nombre.trim(),
                descripcionCorta = actual.descripcionCorta.trim(),
                explicacionProceso = actual.explicacionProceso.trim().ifBlank { null },
                fotos = actual.fotos,
                pendienteSincronizar = false
            )

            val resultado = if (producto.id <= 0) {
                productoRepository.guardar(producto)
            } else {
                productoRepository.actualizar(producto)
            }

            _uiState.update { it.copy(estaGuardando = false) }

            when (resultado) {
                is Resultado.Exito -> onExito()
                is Resultado.Error -> _uiState.update { it.copy(errorGeneral = resultado.mensaje) }
            }
        }
    }

    fun eliminarProducto(onExito: () -> Unit) {
        val id = _uiState.value.id
        if (id <= 0) return

        viewModelScope.launch {
            when (val res = productoRepository.eliminar(id)) {
                is Resultado.Exito -> onExito()
                is Resultado.Error -> _uiState.update { it.copy(errorGeneral = res.mensaje) }
            }
        }
    }
}
