package com.example.textilcircular.ui.screens.catalog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.components.DialogoConfirmacion
import com.example.textilcircular.ui.components.IndicadorCargando
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider
import com.example.textilcircular.util.Constantes
import java.io.File

// Formulario conectado a ProductoFormViewModel para crear o editar productos con cámara, galería y compresión real.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    productoId: Int?,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(productoId) {
        if (productoId != null && productoId > 0) {
            viewModel.cargarProducto(productoId)
        }
    }

    ProductoFormContenido(
        esModoEdicion = (productoId != null && productoId > 0),
        nombre = uiState.nombre,
        descripcionCorta = uiState.descripcionCorta,
        explicacionProceso = uiState.explicacionProceso,
        fotos = uiState.fotos,
        estaProcesandoFoto = uiState.estaProcesandoFoto,
        mensajeEstadoFoto = uiState.mensajeEstadoFoto,
        errorNombre = uiState.errorNombre,
        errorDescripcion = uiState.errorDescripcion,
        errorProceso = uiState.errorProceso,
        errorFotos = uiState.errorFotos,
        errorGeneral = uiState.errorGeneral,
        onNombreCambiado = viewModel::onNombreCambiado,
        onDescripcionCambiado = viewModel::onDescripcionCambiado,
        onExplicacionProcesoCambiado = viewModel::onExplicacionProcesoCambiado,
        onEliminarFoto = viewModel::eliminarFoto,
        onProcesarFoto = { uri, esTemp -> viewModel.procesarFoto(context, uri, esTemp) },
        onGuardarClick = { viewModel.guardarProducto(onExito = onNavegarAtras) },
        onEliminarClick = { viewModel.eliminarProducto(onExito = onNavegarAtras) },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

// Sobrecarga de compatibilidad para previsualizaciones o integración directa.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    productoExistente: Producto?,
    onGuardarClick: (Producto) -> Unit,
    onEliminarClick: (Int) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esModoEdicion = productoExistente != null
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(productoExistente?.nombre ?: "") }
    var descripcionCorta by remember { mutableStateOf(productoExistente?.descripcionCorta ?: "") }
    var explicacionProceso by remember { mutableStateOf(productoExistente?.explicacionProceso ?: "") }

    val fotos = remember {
        mutableStateListOf<String>().apply {
            if (productoExistente != null) {
                addAll(productoExistente.fotos)
            }
        }
    }

    var estaProcesandoFoto by remember { mutableStateOf(false) }

    ProductoFormContenido(
        esModoEdicion = esModoEdicion,
        nombre = nombre,
        descripcionCorta = descripcionCorta,
        explicacionProceso = explicacionProceso,
        fotos = fotos,
        estaProcesandoFoto = estaProcesandoFoto,
        mensajeEstadoFoto = null,
        errorNombre = null,
        errorDescripcion = null,
        errorProceso = null,
        errorFotos = null,
        errorGeneral = null,
        onNombreCambiado = { nombre = it },
        onDescripcionCambiado = { descripcionCorta = it },
        onExplicacionProcesoCambiado = { explicacionProceso = it },
        onEliminarFoto = { indice -> if (indice in fotos.indices) fotos.removeAt(indice) },
        onProcesarFoto = { _, _ -> },
        onGuardarClick = {
            val producto = Producto(
                id = productoExistente?.id ?: 0,
                nombre = nombre,
                descripcionCorta = descripcionCorta,
                explicacionProceso = explicacionProceso.ifBlank { null },
                fotos = fotos.toList(),
                pendienteSincronizar = true
            )
            onGuardarClick(producto)
        },
        onEliminarClick = {
            if (productoExistente != null) {
                onEliminarClick(productoExistente.id)
            }
        },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

// Componente visual unificado para el formulario de creaciones textiles circulares.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductoFormContenido(
    esModoEdicion: Boolean,
    nombre: String,
    descripcionCorta: String,
    explicacionProceso: String,
    fotos: List<String>,
    estaProcesandoFoto: Boolean,
    mensajeEstadoFoto: String?,
    errorNombre: String?,
    errorDescripcion: String?,
    errorProceso: String?,
    errorFotos: String?,
    errorGeneral: String?,
    onNombreCambiado: (String) -> Unit,
    onDescripcionCambiado: (String) -> Unit,
    onExplicacionProcesoCambiado: (String) -> Unit,
    onEliminarFoto: (Int) -> Unit,
    onProcesarFoto: (Uri, Boolean) -> Unit,
    onGuardarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mostrarHojaOrigenFoto by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoExplicacionPermiso by remember { mutableStateOf(false) }
    var mostrarDialogoIrAAjustes by remember { mutableStateOf(false) }

    // Uri temporal para la captura con cámara mediante FileProvider
    var uriCapturaPendiente by remember { mutableStateOf<Uri?>(null) }
    var archivoTempPendiente by remember { mutableStateOf<File?>(null) }

    val sheetState = rememberModalBottomSheetState()

    // Lanzador para captura fotográfica con la cámara del dispositivo
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { capturaExitosa ->
        val uri = uriCapturaPendiente
        if (capturaExitosa && uri != null) {
            onProcesarFoto(uri, true)
        } else {
            archivoTempPendiente?.delete()
        }
        uriCapturaPendiente = null
        archivoTempPendiente = null
    }

    // Función auxiliar para iniciar la toma de fotografía mediante FileProvider
    val iniciarCapturaCamara = {
        try {
            val carpetaTemp = File(context.cacheDir, "fotos_temp").apply { mkdirs() }
            val archivoTemp = File.createTempFile("captura_", ".jpg", carpetaTemp)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivoTemp
            )
            archivoTempPendiente = archivoTemp
            uriCapturaPendiente = uri
            camaraLauncher.launch(uri)
        } catch (e: Exception) {
            // Manejo de error al crear el archivo temporal
        }
    }

    // Lanzador para solicitar permiso de cámara en tiempo de ejecución
    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            iniciarCapturaCamara()
        } else {
            val actividad = context as? Activity
            val debeMostrarExplicacion = actividad?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false

            if (!debeMostrarExplicacion) {
                mostrarDialogoIrAAjustes = true
            } else {
                mostrarDialogoExplicacionPermiso = true
            }
        }
    }

    // Lanzador para selección desde galería del dispositivo
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uriSeleccionada: Uri? ->
        if (uriSeleccionada != null) {
            onProcesarFoto(uriSeleccionada, false)
        }
    }

    // Función que evalúa permisos antes de abrir la cámara
    val solicitarTomaFoto = {
        mostrarHojaOrigenFoto = false
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            iniciarCapturaCamara()
        } else {
            val actividad = context as? Activity
            val debeMostrarExplicacion = actividad?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false

            if (debeMostrarExplicacion) {
                mostrarDialogoExplicacionPermiso = true
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (esModoEdicion) {
                    stringResource(id = R.string.producto_form_titulo_editar)
                } else {
                    stringResource(id = R.string.producto_form_titulo_crear)
                },
                onNavegarAtras = onNavegarAtras
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (errorGeneral != null) {
                    Text(
                        text = errorGeneral,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                BotonPrincipal(
                    texto = stringResource(id = R.string.accion_guardar),
                    onClick = onGuardarClick,
                    habilitado = nombre.isNotBlank() && descripcionCorta.isNotBlank() && !estaProcesandoFoto
                )
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Sección de fotografías del producto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.producto_form_fotos_seccion),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(
                        id = R.string.producto_fotos_contador,
                        fotos.size,
                        Constantes.MAX_FOTOS_POR_PRODUCTO
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = stringResource(id = R.string.producto_form_fotos_ayuda, Constantes.MAX_FOTOS_POR_PRODUCTO),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Indicador de procesamiento y compresión real en segundo plano
            if (estaProcesandoFoto) {
                IndicadorCargando(
                    mensaje = mensajeEstadoFoto ?: stringResource(id = R.string.estado_preparando_foto),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón para agregar nueva foto respetando auditoría de límites (máx 5 fotos)
                    if (fotos.size < Constantes.MAX_FOTOS_POR_PRODUCTO) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { mostrarHojaOrigenFoto = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(id = R.string.producto_form_boton_agregar_foto),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Renderizado de fotos procesadas o imágenes locales existentes
                    itemsIndexed(fotos) { index, rutaFoto ->
                        val bitmap = remember(rutaFoto) {
                            val archivo = File(rutaFoto)
                            if (archivo.exists()) {
                                BitmapFactory.decodeFile(archivo.absolutePath)
                            } else null
                        }

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(
                                onClick = { onEliminarFoto(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.cd_icono_borrar),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (errorFotos != null) {
                Text(
                    text = errorFotos,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Nombre con auditoría de límites
            CampoTexto(
                valor = nombre,
                onValorCambiado = onNombreCambiado,
                etiqueta = stringResource(id = R.string.producto_form_campo_nombre),
                textoAyuda = errorNombre ?: stringResource(id = R.string.producto_form_campo_nombre_ayuda),
                esError = errorNombre != null
            )

            // Campo Descripción corta con auditoría de límites
            CampoTexto(
                valor = descripcionCorta,
                onValorCambiado = onDescripcionCambiado,
                etiqueta = stringResource(id = R.string.producto_form_campo_descripcion),
                textoAyuda = errorDescripcion ?: stringResource(id = R.string.producto_form_campo_descripcion_ayuda),
                esError = errorDescripcion != null,
                lineasMaximas = 3
            )

            // Campo opcional para explicar la técnica textil utilizada
            CampoTexto(
                valor = explicacionProceso,
                onValorCambiado = onExplicacionProcesoCambiado,
                etiqueta = stringResource(id = R.string.producto_form_campo_proceso),
                textoAyuda = errorProceso ?: stringResource(id = R.string.producto_form_campo_proceso_ayuda),
                esError = errorProceso != null,
                lineasMaximas = 4
            )

            // Opción de eliminar en modo edición
            if (esModoEdicion) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { mostrarDialogoEliminar = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.producto_form_eliminar_boton),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Hoja inferior para seleccionar origen de la fotografía (cámara o galería)
        if (mostrarHojaOrigenFoto) {
            ModalBottomSheet(
                onDismissRequest = { mostrarHojaOrigenFoto = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.producto_form_hoja_titulo),
                        style = MaterialTheme.typography.titleLarge
                    )

                    BotonPrincipal(
                        texto = stringResource(id = R.string.accion_tomar_foto),
                        onClick = { solicitarTomaFoto() }
                    )

                    BotonSecundario(
                        texto = stringResource(id = R.string.accion_elegir_galeria),
                        onClick = {
                            mostrarHojaOrigenFoto = false
                            galeriaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Diálogo explicativo previo a solicitar permiso de cámara
        if (mostrarDialogoExplicacionPermiso) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoExplicacionPermiso = false },
                title = { Text(stringResource(id = R.string.permiso_camara_titulo)) },
                text = { Text(stringResource(id = R.string.permiso_camara_mensaje)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoExplicacionPermiso = false
                            permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text(stringResource(id = R.string.permiso_camara_boton_permitir))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoExplicacionPermiso = false }) {
                        Text(stringResource(id = R.string.accion_cancelar))
                    }
                }
            )
        }

        // Diálogo con botón para ir a Ajustes del sistema si se denegó permanentemente
        if (mostrarDialogoIrAAjustes) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoIrAAjustes = false },
                title = { Text(stringResource(id = R.string.permiso_camara_denegado_titulo)) },
                text = { Text(stringResource(id = R.string.permiso_camara_denegado_mensaje)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoIrAAjustes = false
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Text(stringResource(id = R.string.permiso_camara_boton_ajustes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoIrAAjustes = false }) {
                        Text(stringResource(id = R.string.accion_cancelar))
                    }
                }
            )
        }

        // Diálogo de confirmación para eliminar
        if (mostrarDialogoEliminar) {
            DialogoConfirmacion(
                titulo = stringResource(id = R.string.producto_form_eliminar_titulo),
                mensaje = stringResource(id = R.string.producto_form_eliminar_mensaje),
                textoConfirmar = stringResource(id = R.string.accion_eliminar),
                esAccionPeligrosa = true,
                onConfirmar = {
                    mostrarDialogoEliminar = false
                    onEliminarClick()
                },
                onCancelar = { mostrarDialogoEliminar = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductoFormScreenCrearPreview() {
    TextilCircularTheme {
        ProductoFormScreen(
            productoExistente = null,
            onGuardarClick = {},
            onEliminarClick = {},
            onNavegarAtras = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductoFormScreenEditarPreview() {
    TextilCircularTheme {
        ProductoFormScreen(
            productoExistente = DatosDemo.ProductosDemo.first(),
            onGuardarClick = {},
            onEliminarClick = {},
            onNavegarAtras = {}
        )
    }
}
