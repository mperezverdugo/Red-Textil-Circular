package com.example.textilcircular.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.ItemSincronizacion
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.IndicadorCargando
import com.example.textilcircular.ui.components.InsigniaPendiente
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.VerdeExito
import com.example.textilcircular.ui.theme.VerdeExitoFondo
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla de sincronización: muestra los datos pendientes de respaldo y permite enviarlos.
@Composable
fun SincronizacionScreen(
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SincronizacionContent(
        itemsPendientes = uiState.itemsPendientes,
        estadoSync = uiState.estadoSincronizacion,
        onForzarSincronizacion = { viewModel.forzarSincronizacion() },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

@Composable
fun SincronizacionContent(
    itemsPendientes: List<ItemSincronizacion>,
    estadoSync: EstadoSincronizacion,
    onForzarSincronizacion: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.sync_titulo),
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
                    texto = stringResource(id = R.string.sync_boton_sincronizar),
                    onClick = onForzarSincronizacion,
                    habilitado = estadoSync != EstadoSincronizacion.SINCRONIZANDO && itemsPendientes.isNotEmpty()
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
            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta de última sincronización
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.sync_ultima_fecha),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Estados de ejecución
            when (estadoSync) {
                EstadoSincronizacion.SINCRONIZANDO -> {
                    IndicadorCargando(
                        mensaje = stringResource(id = R.string.estado_sincronizando),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }

                EstadoSincronizacion.SINCRONIZADO -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VerdeExitoFondo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VerdeExito),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = R.string.sync_estado_completado),
                                style = MaterialTheme.typography.bodyLarge,
                                color = VerdeExito
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                else -> {}
            }

            // Lista de elementos pendientes
            Text(
                text = stringResource(id = R.string.sync_pendientes_titulo, itemsPendientes.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (itemsPendientes.isEmpty() && estadoSync != EstadoSincronizacion.SINCRONIZANDO) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = stringResource(id = R.string.sync_sin_pendientes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(itemsPendientes, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.tipoElemento,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.detalle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                InsigniaPendiente()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SincronizacionScreenPreview() {
    TextilCircularTheme {
        SincronizacionContent(
            itemsPendientes = DatosDemo.ElementosPendientesSyncDemo,
            estadoSync = EstadoSincronizacion.PENDIENTE,
            onForzarSincronizacion = {},
            onNavegarAtras = {}
        )
    }
}
