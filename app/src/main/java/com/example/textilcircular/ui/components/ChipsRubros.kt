package com.example.textilcircular.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.util.Constantes

/**
 * Componente para selección de rubros mediante Chips (RF-04).
 * Itera dinámicamente sobre Rubro.entries para soportar automáticamente
 * la inclusión de los 2 rubros pendientes de la Fundación.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsRubros(
    rubrosSeleccionados: List<Rubro>,
    onRubroToggled: (Rubro) -> Unit,
    modifier: Modifier = Modifier,
    maxRubros: Int = Constantes.MAX_RUBROS
) {
    val totalSeleccionados = rubrosSeleccionados.size

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                id = R.string.rubros_contador,
                maxRubros,
                totalSeleccionados,
                maxRubros
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Rubro.entries.forEach { rubro ->
                val estaSeleccionado = rubrosSeleccionados.contains(rubro)
                val puedeSeleccionar = estaSeleccionado || totalSeleccionados < maxRubros

                FilterChip(
                    selected = estaSeleccionado,
                    onClick = {
                        if (estaSeleccionado || puedeSeleccionar) {
                            onRubroToggled(rubro)
                        }
                    },
                    label = {
                        Text(
                            text = rubro.nombre,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = if (estaSeleccionado) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(id = R.string.cd_icono_exito),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else null,
                    enabled = puedeSeleccionar,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        if (totalSeleccionados >= maxRubros) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.rubros_limite_alcanzado),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChipsRubrosPreview() {
    TextilCircularTheme {
        ChipsRubros(
            rubrosSeleccionados = listOf(Rubro.SUPRARRECICLAJE),
            onRubroToggled = {}
        )
    }
}
