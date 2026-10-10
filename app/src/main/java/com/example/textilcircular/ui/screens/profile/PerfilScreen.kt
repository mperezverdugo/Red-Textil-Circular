package com.example.textilcircular.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Pantalla 12: Mi Perfil
 * Muestra la información personal, accesos a gestión de cuenta y panel de demostración para evaluación.
 */
@Composable
fun PerfilScreen(
    emprendedora: Emprendedora,
    modoSinConexion: Boolean,
    onModoSinConexionCambiado: (Boolean) -> Unit,
    simularListasVacias: Boolean,
    onSimularListasVaciasCambiado: (Boolean) -> Unit,
    onEditarPerfilClick: () -> Unit,
    onVistaPreviaClick: () -> Unit,
    onSincronizacionClick: () -> Unit,
    onBajaClick: () -> Unit,
    onCerrarSesionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(titulo = stringResource(id = R.string.perfil_titulo))
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

            // Cabecera de Ficha de la Emprendedora
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = emprendedora.nombreComercial,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = emprendedora.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "RUT: ${emprendedora.rut}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Correo: ${emprendedora.email}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ubicación: ${emprendedora.comuna}, Región de ${emprendedora.region}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rubros: " + emprendedora.rubros.joinToString { it.nombre },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!emprendedora.instagram.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Instagram: @${emprendedora.instagram}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Opciones de Gestión de la Cuenta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    FilaOpcionMenu(
                        icono = Icons.Default.Edit,
                        texto = stringResource(id = R.string.perfil_opcion_editar),
                        onClick = onEditarPerfilClick
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    FilaOpcionMenu(
                        icono = Icons.Default.Info,
                        texto = stringResource(id = R.string.perfil_opcion_vista_previa),
                        onClick = onVistaPreviaClick
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    FilaOpcionMenu(
                        icono = Icons.Default.Refresh,
                        texto = stringResource(id = R.string.perfil_opcion_sincronizacion),
                        onClick = onSincronizacionClick
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    FilaOpcionMenu(
                        icono = Icons.Default.Close,
                        texto = stringResource(id = R.string.perfil_opcion_baja),
                        onClick = onBajaClick,
                        esPeligroso = true
                    )
                }
            }

            // TODO: Eliminar sección de opciones de demostración al terminar el proyecto final
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.perfil_seccion_demo_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.perfil_demo_offline),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = modoSinConexion,
                            onCheckedChange = onModoSinConexionCambiado
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.perfil_demo_listas_vacias),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = simularListasVacias,
                            onCheckedChange = onSimularListasVaciasCambiado
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BotonSecundario(
                texto = stringResource(id = R.string.accion_cerrar_sesion),
                onClick = onCerrarSesionClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FilaOpcionMenu(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    esPeligroso: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (esPeligroso) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = if (esPeligroso) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    TextilCircularTheme {
        PerfilScreen(
            emprendedora = DatosDemo.EmprendedoraDemo,
            modoSinConexion = false,
            onModoSinConexionCambiado = {},
            simularListasVacias = false,
            onSimularListasVaciasCambiado = {},
            onEditarPerfilClick = {},
            onVistaPreviaClick = {},
            onSincronizacionClick = {},
            onBajaClick = {},
            onCerrarSesionClick = {}
        )
    }
}
