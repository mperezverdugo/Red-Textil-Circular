package com.example.textilcircular.ui.screens.admin

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.AzulRevision
import com.example.textilcircular.ui.theme.AzulRevisionFondo
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Panel administrativo: métricas de impacto, contactos por región y rubro.
@Composable
fun PanelAdminScreen(
    onVerFichasPorRevisarClick: () -> Unit,
    modifier: Modifier = Modifier,
    fichasPorRevisar: Int = DatosDemo.FichasPorRevisarContador,
    totalBusquedas: Int = 1420,
    totalConsultas: Int = 385,
    totalClics: Int = 890,
    contactosPorRegion: List<Pair<String, Int>> = DatosDemo.ContactosPorRegionDemo,
    contactosPorRubro: List<Pair<String, Int>> = DatosDemo.ContactosPorRubroDemo
) {
    Scaffold(
        topBar = {
            BarraSuperior(titulo = stringResource(id = R.string.admin_panel_titulo))
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

            Text(
                text = stringResource(id = R.string.admin_panel_bajada),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Tarjetas de métricas de uso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaMetricaResumen(
                    titulo = stringResource(id = R.string.admin_metrica_busquedas),
                    valor = totalBusquedas.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetricaResumen(
                    titulo = stringResource(id = R.string.admin_metrica_consultas),
                    valor = totalConsultas.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetricaResumen(
                    titulo = stringResource(id = R.string.admin_metrica_clics),
                    valor = totalClics.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Alerta de fichas pendientes de revisión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVerFichasPorRevisarClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulRevisionFondo)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AzulRevision),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.admin_alerta_fichas_revisar),
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulRevision
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.admin_alerta_fichas_desc, fichasPorRevisar),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AzulRevision
                        )
                    }
                }
            }

            // Gráfico de contactos por región
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.admin_grafico_region_titulo),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val valorMaxRegion = (contactosPorRegion.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1).toFloat()

                    contactosPorRegion.forEach { (region, cantidad) ->
                        val proporcion = (cantidad / valorMaxRegion).coerceIn(0.1f, 1f)
                        FilaBarraGrafico(
                            etiqueta = region,
                            cantidad = cantidad,
                            proporcion = proporcion,
                            colorBarra = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Gráfico de contactos por rubro
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.admin_grafico_rubro_titulo),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val valorMaxRubro = (contactosPorRubro.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1).toFloat()

                    contactosPorRubro.forEach { (rubro, cantidad) ->
                        val proporcion = (cantidad / valorMaxRubro).coerceIn(0.1f, 1f)
                        FilaBarraGrafico(
                            etiqueta = rubro,
                            cantidad = cantidad,
                            proporcion = proporcion,
                            colorBarra = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaMetricaResumen(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaBarraGrafico(
    etiqueta: String,
    cantidad: Int,
    proporcion: Float,
    colorBarra: androidx.compose.ui.graphics.Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$cantidad contactos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Barra de progreso visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(proporcion)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(colorBarra)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PanelAdminScreenPreview() {
    TextilCircularTheme {
        PanelAdminScreen(onVerFichasPorRevisarClick = {})
    }
}
