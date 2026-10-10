package com.example.textilcircular.ui.screens.home

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.InsigniaEstado
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Pantalla 5: Inicio de la Emprendedora
 * Panel central con estado de visibilidad, progreso del perfil (RF-34), consultas y accesos rápidos.
 */
@Composable
fun InicioScreen(
    emprendedora: Emprendedora,
    onAgregarProductoClick: () -> Unit,
    onVerMensajesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Saludo personalizado con lenguaje cálido y sencillo
        val primerNombre = emprendedora.nombre.split(" ").firstOrNull() ?: emprendedora.nombre
        Text(
            text = stringResource(id = R.string.inicio_saludo, primerNombre),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(id = R.string.inicio_bajada),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 1. Tarjeta: Estado en el Directorio Web
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
                        text = stringResource(id = R.string.inicio_tarjeta_estado_titulo),
                        style = MaterialTheme.typography.titleMedium
                    )
                    InsigniaEstado(
                        estado = if (emprendedora.activa) EstadoFichaEmprendimiento.ACTIVA else EstadoFichaEmprendimiento.OCULTA
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (emprendedora.activa) {
                        stringResource(id = R.string.inicio_tarjeta_estado_visible)
                    } else {
                        stringResource(id = R.string.inicio_tarjeta_estado_oculta)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Tarjeta: "Completa tu perfil" (RF-34)
        val avancePerfil = 75
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
                        text = stringResource(id = R.string.inicio_completar_perfil_titulo),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(id = R.string.inicio_completar_perfil_avance, avancePerfil),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { avancePerfil / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )

                Spacer(modifier = Modifier.height(10.dp))

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
        val consultasNuevas = 1
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVerMensajesClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. Acción principal destacada: "Agregar un producto" (máximo 3 toques según RNF-08)
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
