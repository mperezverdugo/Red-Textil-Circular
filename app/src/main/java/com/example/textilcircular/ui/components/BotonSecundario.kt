package com.example.textilcircular.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.ui.theme.BlancoLino
import com.example.textilcircular.ui.theme.GrisTextoSecundario
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaFundacion

/**
 * Botón secundario para acciones opcionales o navegación de retroceso.
 * Integra la estética textil artesanal:
 * - Fondo en fibra de lino clara (BlancoLino #FAF8F5).
 * - Borde pespunteado punteado tipo hilván fino.
 * - Micro-animación elástica suave (spring) que comprime a 0.97f al presionar.
 *
 * Mantiene intactos todos los parámetros originales de la firma.
 */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Micro-animación elástica de pulsación
    val escalaAnimada by animateFloatAsState(
        targetValue = if (isPressed && habilitado) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "escalaBotonSecundario"
    )

    val colorBordeHilo = if (habilitado) TurquesaFundacion else MaterialTheme.colorScheme.outline

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .graphicsLayer {
                scaleX = escalaAnimada
                scaleY = escalaAnimada
            }
            .drawWithContent {
                drawContent()

                // Pespunte perimetral sutil en hilo turquesa o hilván
                val strokeWidthPx = 1.5.dp.toPx()
                val puntadaPx = 5.dp.toPx()
                val separacionPx = 3.dp.toPx()
                val radioEsquinaPx = 8.dp.toPx()
                val mitadGrosor = strokeWidthPx / 2f

                drawRoundRect(
                    color = colorBordeHilo,
                    topLeft = Offset(mitadGrosor, mitadGrosor),
                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                    cornerRadius = CornerRadius(radioEsquinaPx, radioEsquinaPx),
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(puntadaPx, separacionPx), 0f)
                    )
                )
            },
        enabled = habilitado,
        shape = RoundedCornerShape(10.dp),
        interactionSource = interactionSource,
        border = null, // Reemplazado por el pespunte artesanal personalizado
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BlancoLino,
            contentColor = TurquesaFundacion,
            disabledContentColor = GrisTextoSecundario
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BotonSecundarioPreview() {
    TextilCircularTheme {
        BotonSecundario(texto = "Volver atrás", onClick = {})
    }
}
