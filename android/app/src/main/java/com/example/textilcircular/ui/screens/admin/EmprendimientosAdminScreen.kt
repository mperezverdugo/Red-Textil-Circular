package com.example.textilcircular.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.InsigniaEstado
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Directorio administrativo para buscar y supervisar emprendimientos.
@Composable
fun EmprendimientosAdminScreen(
    fichas: List<FichaAdminEmprendimiento>,
    onEmprendimientoClick: (Int) -> Unit,
    onAgregarEmprendimientoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroEstadoSeleccionado by remember { mutableStateOf<EstadoFichaEmprendimiento?>(null) }

    val fichasFiltradas = fichas.filter { ficha ->
        val coincideTexto = textoBusqueda.isBlank() ||
                ficha.emprendedora.nombreComercial.contains(textoBusqueda, ignoreCase = true) ||
                ficha.emprendedora.nombre.contains(textoBusqueda, ignoreCase = true) ||
                ficha.emprendedora.comuna.contains(textoBusqueda, ignoreCase = true)

        val coincideEstado = filtroEstadoSeleccionado == null || ficha.estadoFicha == filtroEstadoSeleccionado

        coincideTexto && coincideEstado
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = stringResource(id = R.string.admin_emp_titulo))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarEmprendimientoClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.admin_emp_boton_agregar)
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Buscador de texto
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.admin_emp_buscar_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.cd_icono_buscar),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                shape = FormaPildora,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros rápidos por estado
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = filtroEstadoSeleccionado == null,
                        onClick = { filtroEstadoSeleccionado = null },
                        shape = FormaPildora,
                        label = { Text(text = stringResource(id = R.string.admin_emp_filtro_todos)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                items(EstadoFichaEmprendimiento.entries) { estado ->
                    FilterChip(
                        selected = filtroEstadoSeleccionado == estado,
                        onClick = { filtroEstadoSeleccionado = estado },
                        shape = FormaPildora,
                        label = {
                            val textoRes = when (estado) {
                                EstadoFichaEmprendimiento.ACTIVA -> R.string.ficha_estado_activa
                                EstadoFichaEmprendimiento.OCULTA -> R.string.ficha_estado_oculta
                                EstadoFichaEmprendimiento.POR_REVISAR -> R.string.ficha_estado_por_revisar
                                EstadoFichaEmprendimiento.RECHAZADA -> R.string.ficha_estado_rechazada
                                EstadoFichaEmprendimiento.DADA_DE_BAJA -> R.string.ficha_estado_dada_de_baja
                            }
                            Text(text = stringResource(id = textoRes))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.admin_emp_total, fichasFiltradas.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de fichas
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(fichasFiltradas, key = { it.emprendedora.id }) { ficha ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEmprendimientoClick(ficha.emprendedora.id) },
                        shape = FormaTarjeta,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ficha.emprendedora.nombreComercial,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ficha.emprendedora.nombre} · ${ficha.emprendedora.comuna}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ficha.emprendedora.rubros.joinToString { it.nombre },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            InsigniaEstado(estado = ficha.estadoFicha)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmprendimientosAdminScreenPreview() {
    TextilCircularTheme {
        EmprendimientosAdminScreen(
            fichas = DatosDemo.EmprendimientosAdminDemo,
            onEmprendimientoClick = {},
            onAgregarEmprendimientoClick = {}
        )
    }
}
