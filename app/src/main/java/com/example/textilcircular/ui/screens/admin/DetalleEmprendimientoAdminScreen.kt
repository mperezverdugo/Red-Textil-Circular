package com.example.textilcircular.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.DialogoConfirmacion
import com.example.textilcircular.ui.components.InsigniaEstado
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.AzulRevision
import com.example.textilcircular.ui.theme.AzulRevisionFondo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.VerdeExito
import com.example.textilcircular.ui.theme.VerdeExitoFondo

// Detalle de un emprendimiento desde el perfil administrador, con opciones de moderación.
@Composable
fun DetalleEmprendimientoAdminScreen(
    ficha: FichaAdminEmprendimiento,
    onEditarClick: (Int) -> Unit,
    onCambiarVisibilidadClick: (Int) -> Unit,
    onEliminarClick: (Int) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.admin_detalle_titulo),
                onNavegarAtras = onNavegarAtras
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonPrincipal(
                    texto = stringResource(id = R.string.accion_editar),
                    onClick = { onEditarClick(ficha.emprendedora.id) }
                )

                val esActiva = ficha.estadoFicha == EstadoFichaEmprendimiento.ACTIVA
                BotonSecundario(
                    texto = stringResource(
                        id = if (esActiva) R.string.admin_detalle_boton_ocultar else R.string.admin_detalle_boton_mostrar
                    ),
                    onClick = { onCambiarVisibilidadClick(ficha.emprendedora.id) }
                )

                OutlinedButton(
                    onClick = { mostrarDialogoEliminar = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(id = R.string.accion_eliminar),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
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

            // Cabecera con estado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ficha.emprendedora.nombreComercial,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        InsigniaEstado(estado = ficha.estadoFicha)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Titular: ${ficha.emprendedora.nombre}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "RUT: ${ficha.emprendedora.rut}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Correo: ${ficha.emprendedora.email}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ubicación: ${ficha.emprendedora.comuna}, ${ficha.emprendedora.region}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val rubrosTexto = if (ficha.emprendedora.rubros.isEmpty()) {
                        "Sin rubros"
                    } else {
                        ficha.emprendedora.rubros.joinToString { it.nombre }
                    }
                    Text(
                        text = "Rubros: $rubrosTexto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (ficha.emprendedora.rubros.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                    if (!ficha.emprendedora.instagram.isNullOrBlank()) {
                        Text(
                            text = "Instagram: @${ficha.emprendedora.instagram}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val problemasAuditoria = remember(ficha) {
                val auditados = com.example.textilcircular.util.validacion.AuditorFichas.auditar(ficha.emprendedora)
                if (auditados.isNotEmpty()) auditados else ficha.problemasDetectados
            }

            // Panel de inconsistencias detectadas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (problemasAuditoria.isNotEmpty()) AzulRevisionFondo else VerdeExitoFondo
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (problemasAuditoria.isNotEmpty()) Icons.Default.Warning else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (problemasAuditoria.isNotEmpty()) AzulRevision else VerdeExito,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                id = if (problemasAuditoria.isNotEmpty()) {
                                    R.string.admin_detalle_problemas_titulo
                                } else {
                                    R.string.admin_detalle_estado_consistente_titulo
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (problemasAuditoria.isNotEmpty()) AzulRevision else VerdeExito
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (problemasAuditoria.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.admin_detalle_sin_problemas),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerdeExito
                        )
                    } else {
                        problemasAuditoria.forEach { problema ->
                            Text(
                                text = "• $problema",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AzulRevision
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (mostrarDialogoEliminar) {
            DialogoConfirmacion(
                titulo = stringResource(id = R.string.admin_detalle_eliminar_titulo),
                mensaje = stringResource(id = R.string.admin_detalle_eliminar_mensaje),
                textoConfirmar = stringResource(id = R.string.accion_eliminar),
                esAccionPeligrosa = true,
                onConfirmar = {
                    mostrarDialogoEliminar = false
                    onEliminarClick(ficha.emprendedora.id)
                },
                onCancelar = { mostrarDialogoEliminar = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleEmprendimientoAdminScreenPreview() {
    TextilCircularTheme {
        DetalleEmprendimientoAdminScreen(
            ficha = DatosDemo.EmprendimientosAdminDemo[1],
            onEditarClick = {},
            onCambiarVisibilidadClick = {},
            onEliminarClick = {},
            onNavegarAtras = {}
        )
    }
}
