package com.example.textilcircular.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.RegistroAutomatizacionCorreo
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.AzulRevision
import com.example.textilcircular.ui.theme.AzulRevisionFondo
import com.example.textilcircular.ui.theme.RojoError
import com.example.textilcircular.ui.theme.RojoErrorFondo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.VerdeExito
import com.example.textilcircular.ui.theme.VerdeExitoFondo

// Supervisión del envío automático de correos y reintento ante fallos.
@Composable
fun AutomatizacionesAdminScreen(
    modifier: Modifier = Modifier
) {
    val listaCorreos = remember {
        mutableStateListOf<RegistroAutomatizacionCorreo>().apply {
            addAll(DatosDemo.AutomatizacionesCorreosDemo)
        }
    }

    val totalEnviados = listaCorreos.count { it.estado == EstadoCorreo.ENVIADO }
    val totalPendientes = listaCorreos.count { it.estado == EstadoCorreo.PENDIENTE }
    val totalFallidos = listaCorreos.count { it.estado == EstadoCorreo.FALLIDO }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = stringResource(id = R.string.admin_auto_titulo))
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

            Text(
                text = stringResource(id = R.string.admin_auto_bajada),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Resumen de estados de automatización
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaContadorCorreo(
                    texto = stringResource(id = R.string.admin_auto_total_enviados, totalEnviados),
                    colorFondo = VerdeExitoFondo,
                    colorTexto = VerdeExito,
                    modifier = Modifier.weight(1f)
                )
                TarjetaContadorCorreo(
                    texto = stringResource(id = R.string.admin_auto_total_pendientes, totalPendientes),
                    colorFondo = AzulRevisionFondo,
                    colorTexto = AzulRevision,
                    modifier = Modifier.weight(1f)
                )
                TarjetaContadorCorreo(
                    texto = stringResource(id = R.string.admin_auto_total_fallidos, totalFallidos),
                    colorFondo = RojoErrorFondo,
                    colorTexto = RojoError,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de correos auditados
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listaCorreos, key = { it.id }) { registro ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = registro.emprendimiento,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                InsigniaEstadoCorreo(estado = registro.estado)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = registro.destinatario,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = registro.fechaEnvio,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Botón de reintento para correos fallidos
                            if (registro.estado == EstadoCorreo.FALLIDO) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        val idx = listaCorreos.indexOfFirst { it.id == registro.id }
                                        if (idx >= 0) {
                                            listaCorreos[idx] = registro.copy(
                                                estado = EstadoCorreo.ENVIADO,
                                                fechaEnvio = "Reenviado hace un momento"
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(id = R.string.accion_reenviar),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
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

@Composable
private fun TarjetaContadorCorreo(
    texto: String,
    colorFondo: androidx.compose.ui.graphics.Color,
    colorTexto: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(colorFondo, RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = colorTexto
        )
    }
}

@Composable
private fun InsigniaEstadoCorreo(
    estado: EstadoCorreo
) {
    val (colorFondo, colorTexto, icono, etiqueta) = when (estado) {
        EstadoCorreo.ENVIADO -> Tuple4(VerdeExitoFondo, VerdeExito, Icons.Default.Check, "Enviado")
        EstadoCorreo.PENDIENTE -> Tuple4(AzulRevisionFondo, AzulRevision, Icons.Default.Refresh, "Pendiente")
        EstadoCorreo.FALLIDO -> Tuple4(RojoErrorFondo, RojoError, Icons.Default.Close, "Falló")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorFondo, RoundedCornerShape(12.dp))
            .border(1.dp, colorTexto, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorTexto,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = colorTexto
        )
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Preview(showBackground = true)
@Composable
fun AutomatizacionesAdminScreenPreview() {
    TextilCircularTheme {
        AutomatizacionesAdminScreen()
    }
}
