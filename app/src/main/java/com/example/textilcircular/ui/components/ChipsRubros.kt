package com.example.textilcircular.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.theme.BlancoLino
import com.example.textilcircular.ui.theme.GrisBorde
import com.example.textilcircular.ui.theme.NegroTexto
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaClaro
import com.example.textilcircular.ui.theme.TurquesaFundacion
import com.example.textilcircular.ui.theme.TurquesaOscuro
import com.example.textilcircular.util.Constantes

/**
 * Componente para selección de rubros mediante Chips (RF-04).
 * Rediseñado como etiqueta de tela (fabric tag):
 * - Transición suave de color (`animateColorAsState`) al seleccionar/deseleccionar.
 * - Bordes redondeados con contraste accesible WCAG AA.
 * - Soporte dinámico para `Rubro.entries`.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
            fontWeight = FontWeight.Bold,
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

                // Transiciones suaves de color tipo etiqueta textil
                val colorFondo by animateColorAsState(
                    targetValue = if (estaSeleccionado) TurquesaClaro else BlancoLino,
                    animationSpec = tween(durationMillis = 250),
                    label = "colorFondoChip"
                )
                val colorTexto by animateColorAsState(
                    targetValue = if (estaSeleccionado) TurquesaOscuro else NegroTexto,
                    animationSpec = tween(durationMillis = 250),
                    label = "colorTextoChip"
                )
                val colorBorde by animateColorAsState(
                    targetValue = if (estaSeleccionado) TurquesaFundacion else GrisBorde,
                    animationSpec = tween(durationMillis = 250),
                    label = "colorBordeChip"
                )

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
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (estaSeleccionado) FontWeight.SemiBold else FontWeight.Normal,
                            color = colorTexto
                        )
                    },
                    leadingIcon = if (estaSeleccionado) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(id = R.string.cd_icono_exito),
                                tint = TurquesaFundacion
                            )
                        }
                    } else null,
                    enabled = puedeSeleccionar,
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = puedeSeleccionar,
                        selected = estaSeleccionado,
                        borderColor = colorBorde,
                        selectedBorderColor = colorBorde,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.5.dp
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colorFondo,
                        selectedContainerColor = colorFondo
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

@OptIn(ExperimentalMaterial3Api::class)
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
