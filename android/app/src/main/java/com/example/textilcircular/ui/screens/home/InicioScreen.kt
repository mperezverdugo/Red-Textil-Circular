package com.example.textilcircular.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.Rol
import com.example.textilcircular.ui.components.AvatarUsuario
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.InsigniaEstado
import com.example.textilcircular.ui.components.fondoTramaTextil
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaFundacion

// Pantalla de inicio con resumen del perfil, consultas pendientes y accesos directos.
@Composable
fun InicioScreen(
    emprendedora: Emprendedora,
    onAgregarProductoClick: () -> Unit,
    onVerMensajesClick: () -> Unit,
    modifier: Modifier = Modifier,
    consultasNuevas: Int = 0,
    onVerEducacionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .fondoTramaTextil(alpha = 0.03f)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Saludo personalizado estilo editorial con avatar de usuario a la derecha (estilo referencia)
        val primerNombre = emprendedora.nombre.split(" ").firstOrNull() ?: emprendedora.nombre
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bienvenida 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = primerNombre,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            AvatarUsuario(
                rol = Rol.EMPRENDEDORA,
                tamano = 48.dp
            )
        }

        Text(
            text = stringResource(id = R.string.inicio_bajada),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 1. Tarjeta: Estado en el Directorio Web
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.inicio_tarjeta_estado_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    InsigniaEstado(
                        estado = emprendedora.estadoFicha
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (emprendedora.estadoFicha) {
                        EstadoFichaEmprendimiento.ACTIVA -> stringResource(id = R.string.inicio_tarjeta_estado_visible)
                        EstadoFichaEmprendimiento.POR_REVISAR -> stringResource(id = R.string.inicio_tarjeta_estado_por_revisar)
                        EstadoFichaEmprendimiento.RECHAZADA -> stringResource(id = R.string.inicio_tarjeta_estado_rechazada)
                        else -> stringResource(id = R.string.inicio_tarjeta_estado_oculta)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Tarjeta para completar el perfil
        val avancePerfil = 75
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.inicio_completar_perfil_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(id = R.string.inicio_completar_perfil_avance, avancePerfil),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { avancePerfil / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(FormaPildora),
                    color = AmarilloAcento,
                    trackColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.inicio_pendiente_fotos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Tarjeta: Nuevas consultas con acceso al chat
        if (consultasNuevas > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVerMensajesClick() },
                shape = FormaTarjeta,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.inicio_consultas_titulo),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.inicio_consultas_aviso, consultasNuevas),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.inicio_consultas_ver),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 4. Tarjeta de Educación y Sostenibilidad (API Externa de Wikipedia)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVerEducacionClick?.invoke() },
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aprende sobre Textil Circular",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Artículos en vivo desde la API de Wikipedia sobre reciclaje textil y moda sostenible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Ver",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 5. Botón para agregar un producto al catálogo
        BotonPrincipal(
            texto = stringResource(id = R.string.inicio_boton_agregar_producto),
            onClick = onAgregarProductoClick
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun InicioScreenPreview() {
    TextilCircularTheme {
        InicioScreen(
            emprendedora = DatosDemo.EmprendedoraDemo,
            onAgregarProductoClick = {},
            onVerMensajesClick = {}
        )
    }
}
