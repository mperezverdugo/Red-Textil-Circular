package com.example.textilcircular.ui.screens.catalog

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.textilcircular.util.Constantes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla 7: Crear y Editar Producto (RF-13 a RF-17)
 * Permite gestionar fotografías con hoja modal, compresión visual simulada y proceso circular opcional.
 */
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

    var mostrarHojaOrigenFoto by remember { mutableStateOf(false) }
    var simulandoCompresionFoto by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

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
                BotonPrincipal(
                    texto = stringResource(id = R.string.accion_guardar),
                    onClick = {
                        val productoAguardar = Producto(
                            id = productoExistente?.id ?: 0,
                            nombre = nombre,
                            descripcionCorta = descripcionCorta,
                            explicacionProceso = explicacionProceso.ifBlank { null },
                            fotos = fotos.toList(),
                            pendienteSincronizar = true
                        )
                        onGuardarClick(productoAguardar)
                    },
                    habilitado = nombre.isNotBlank() && descripcionCorta.isNotBlank()
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

            // Sección de Fotos (RF-14, RF-15, RF-17)
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

            // Estado de compresión visual (RNF-03)
            if (simulandoCompresionFoto) {
                IndicadorCargando(
                    mensaje = stringResource(id = R.string.estado_preparando_foto),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón para agregar nueva foto si no se superó el límite
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

                    // Fotos cargadas (Placeholders)
                    itemsIndexed(fotos) { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            IconButton(
                                onClick = { fotos.removeAt(index) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Nombre
            CampoTexto(
                valor = nombre,
                onValorCambiado = { nombre = it },
                etiqueta = stringResource(id = R.string.producto_form_campo_nombre),
                textoAyuda = stringResource(id = R.string.producto_form_campo_nombre_ayuda)
            )

            // Campo Descripción corta
            CampoTexto(
                valor = descripcionCorta,
                onValorCambiado = { descripcionCorta = it },
                etiqueta = stringResource(id = R.string.producto_form_campo_descripcion),
                textoAyuda = stringResource(id = R.string.producto_form_campo_descripcion_ayuda),
                lineasMaximas = 3
            )

            // Campo Opcional de Proceso (RF-16)
            CampoTexto(
                valor = explicacionProceso,
                onValorCambiado = { explicacionProceso = it },
                etiqueta = stringResource(id = R.string.producto_form_campo_proceso),
                textoAyuda = stringResource(id = R.string.producto_form_campo_proceso_ayuda),
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

        // Hoja Inferior para elegir origen de foto (RF-17)
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

                    // TODO(Capa 2): Integrar ActivityResultContracts.TakePicture para cámara nativa
                    BotonPrincipal(
                        texto = stringResource(id = R.string.accion_tomar_foto),
                        onClick = {
                            mostrarHojaOrigenFoto = false
                            simulandoCompresionFoto = true
                            coroutineScope.launch {
                                delay(1200)
                                fotos.add("foto_${fotos.size + 1}")
                                simulandoCompresionFoto = false
                            }
                        }
                    )

                    // TODO(Capa 2): Integrar PickVisualMedia para galería nativa
                    BotonSecundario(
                        texto = stringResource(id = R.string.accion_elegir_galeria),
                        onClick = {
                            mostrarHojaOrigenFoto = false
                            simulandoCompresionFoto = true
                            coroutineScope.launch {
                                delay(1200)
                                fotos.add("foto_${fotos.size + 1}")
                                simulandoCompresionFoto = false
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Diálogo de confirmación para eliminar
        if (mostrarDialogoEliminar && productoExistente != null) {
            DialogoConfirmacion(
                titulo = stringResource(id = R.string.producto_form_eliminar_titulo),
                mensaje = stringResource(id = R.string.producto_form_eliminar_mensaje),
                textoConfirmar = stringResource(id = R.string.accion_eliminar),
                esAccionPeligrosa = true,
                onConfirmar = {
                    mostrarDialogoEliminar = false
                    onEliminarClick(productoExistente.id)
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
