package com.example.textilcircular.ui.screens.cliente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.model.Rol
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.components.AvatarUsuario
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.EstadoVacio
import com.example.textilcircular.ui.components.IndicadorCargando
import com.example.textilcircular.ui.components.SelectorDesplegable
import com.example.textilcircular.ui.components.TarjetaMercadoEmprendedora
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.Blanco
import com.example.textilcircular.ui.theme.FormaBoton
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.ui.theme.TurquesaFundacion
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla principal del mercado donde el cliente busca y filtra emprendedoras textiles.
@Composable
fun ExplorarScreen(
    onEmprendedoraClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onVerEducacionClick: (() -> Unit)? = null,
    viewModel: ExplorarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var mostrarFiltros by remember { mutableStateOf(false) }

    val todasRegiones = stringResource(id = R.string.mercado_filtro_todas_regiones)
    val todasComunas = stringResource(id = R.string.mercado_filtro_todas_comunas)
    val todosRubros = stringResource(id = R.string.mercado_filtro_todos_rubros)

    val comunasDisponibles = remember(uiState.regionFiltro) {
        if (uiState.regionFiltro.isBlank()) emptyList()
        else DatosDemo.RegionesConComunas[uiState.regionFiltro] ?: emptyList()
    }

    val hayFiltrosActivos = uiState.queryTexto.isNotBlank() ||
            uiState.regionFiltro.isNotBlank() ||
            uiState.comunaFiltro.isNotBlank() ||
            uiState.rubroFiltro.isNotBlank()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.mercado_titulo),
                acciones = {
                    AvatarUsuario(
                        rol = Rol.CLIENTE,
                        tamano = 36.dp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            // Buscador por texto con barra estilo píldora y botón de filtros redondeado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.queryTexto,
                    onValueChange = viewModel::onQueryTextoChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.mercado_buscar_placeholder),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    shape = FormaPildora,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.ejecutarBusqueda()
                        }
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.queryTexto.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.onQueryTextoChange("")
                                        viewModel.ejecutarBusqueda()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(id = R.string.cd_icono_borrar),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.ejecutarBusqueda()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(id = R.string.cd_icono_buscar),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                // Botón para desplegar u ocultar los filtros avanzados (estilo botón de filtro referencia)
                Button(
                    onClick = { mostrarFiltros = !mostrarFiltros },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mostrarFiltros || hayFiltrosActivos) AmarilloAcento else TurquesaFundacion,
                        contentColor = if (mostrarFiltros || hayFiltrosActivos) MaterialTheme.colorScheme.onSecondary else Blanco
                    ),
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (mostrarFiltros) stringResource(id = R.string.accion_cerrar) else stringResource(id = R.string.accion_filtrar),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Panel de filtros opcionales (región, comuna y rubro)
            AnimatedVisibility(
                visible = mostrarFiltros || hayFiltrosActivos,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SelectorDesplegable(
                        opciones = listOf(todasRegiones) + DatosDemo.RegionesConComunas.keys.toList(),
                        seleccionado = if (uiState.regionFiltro.isBlank()) todasRegiones else uiState.regionFiltro,
                        onOpcionSeleccionada = { seleccion ->
                            val valor = if (seleccion == todasRegiones) "" else seleccion
                            viewModel.onRegionFiltroChange(valor)
                        },
                        etiqueta = stringResource(id = R.string.mercado_filtro_region)
                    )

                    SelectorDesplegable(
                        opciones = listOf(todasComunas) + comunasDisponibles,
                        seleccionado = if (uiState.comunaFiltro.isBlank()) todasComunas else uiState.comunaFiltro,
                        onOpcionSeleccionada = { seleccion ->
                            val valor = if (seleccion == todasComunas) "" else seleccion
                            viewModel.onComunaFiltroChange(valor)
                        },
                        etiqueta = stringResource(id = R.string.mercado_filtro_comuna),
                        habilitado = uiState.regionFiltro.isNotBlank()
                    )

                    SelectorDesplegable(
                        opciones = listOf(todosRubros) + Rubro.entries.map { it.nombre },
                        seleccionado = if (uiState.rubroFiltro.isBlank()) todosRubros else uiState.rubroFiltro,
                        onOpcionSeleccionada = { seleccion ->
                            val valor = if (seleccion == todosRubros) "" else seleccion
                            viewModel.onRubroFiltroChange(valor)
                        },
                        etiqueta = stringResource(id = R.string.mercado_filtro_rubro)
                    )

                    if (hayFiltrosActivos) {
                        OutlinedButton(
                            onClick = viewModel::limpiarFiltros,
                            shape = FormaBoton,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.accion_limpiar_filtros),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Contenido: cargando, vacío o listado de tarjetas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.cargando -> {
                        IndicadorCargando(
                            mensaje = stringResource(id = R.string.estado_cargando),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.emprendedoras.isEmpty() -> {
                        EstadoVacio(
                            titulo = stringResource(id = R.string.mercado_vacio_titulo),
                            mensaje = stringResource(id = R.string.mercado_vacio_mensaje),
                            textoAccion = stringResource(id = R.string.accion_limpiar_filtros),
                            onAccionClick = viewModel::limpiarFiltros,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (onVerEducacionClick != null) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onVerEducacionClick() },
                                        shape = FormaTarjeta,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Aprende sobre Textil Circular",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = "Consulta conceptos de reciclaje textil en vivo desde Wikipedia",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                                )
                                            }
                                            Text(
                                                text = "Ver",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                            items(
                                items = uiState.emprendedoras,
                                key = { it.emprendedora.id }
                            ) { item ->
                                TarjetaMercadoEmprendedora(
                                    item = item,
                                    onClick = { onEmprendedoraClick(item.emprendedora.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
