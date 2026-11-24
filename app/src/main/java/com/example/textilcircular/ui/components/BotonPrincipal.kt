package com.example.textilcircular.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Botón principal de acción primaria (Inspirado en un parche reforzado textil).
 * Ergonomía y accesibilidad:
 * - Altura táctil mínima de 52dp para fácil pulsación manual.
 * - Micro-interacción táctil elástica que comprime el parche a 0.96f.
 * - Elevación dinámica que desciende de 4dp a 1dp para simular presión sobre tejido.
 * - Pespunte interno bordado (dashPathEffect) con transparencia al 55% del color del texto.
 *
 * Mantiene intactos los 4 parámetros originales garantizando retrocompatibilidad total.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    conEfectoCostura: Boolean = true,
    animarPresion: Boolean = true,
    icono: ImageVector? = null,
    colorFondo: Color? = null,
    colorTexto: Color? = null,
    colorHiloCostura: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Escala elástica: simula compresión de parche textil acolchado
    val escalaAnimada by animateFloatAsState(
        targetValue = if (animarPresion && isPressed && habilitado) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "escalaBotonParche"
    )

    // Elevación dinámica: pasa de 4dp en reposo a 1dp presionado para simular que se hunde al pulsar
    val elevacionAnimada by animateDpAsState(
        targetValue = if (animarPresion && isPressed && habilitado) 1.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "elevacionBotonParche"
    )

    val colorContenedor = colorFondo ?: MaterialTheme.colorScheme.primary
    val colorContenido = colorTexto ?: MaterialTheme.colorScheme.onPrimary
    // Pespunte con 55% de opacidad para una integración sutil y elegante sobre el parche
    val colorHilo = (colorHiloCostura ?: colorContenido).copy(alpha = 0.55f)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = escalaAnimada
                scaleY = escalaAnimada
            }
            .drawWithContent {
                drawContent()

                // Pespunte interno artesanal cuando el botón está activo
                if (conEfectoCostura && habilitado) {
                    val grosorTrazoPx = 1.5.dp.toPx()
                    val puntadaPx = 5.dp.toPx()
                    val separacionPx = 3.dp.toPx()
                    val margenInternoPx = 4.dp.toPx()
                    // El radio de la esquina exterior es 10dp; con margen de 4dp el interior es 6dp
                    val radioEsquinaPx = 6.dp.toPx()

                    val efectoCostura = PathEffect.dashPathEffect(
                        floatArrayOf(puntadaPx, separacionPx),
                        0f
                    )

                    val mitadGrosor = grosorTrazoPx / 2f
                    val left = margenInternoPx + mitadGrosor
                    val top = margenInternoPx + mitadGrosor
                    val right = size.width - margenInternoPx - mitadGrosor
                    val bottom = size.height - margenInternoPx - mitadGrosor

                    drawRoundRect(
                        color = colorHilo,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        cornerRadius = CornerRadius(radioEsquinaPx, radioEsquinaPx),
                        style = Stroke(
                            width = grosorTrazoPx,
                            pathEffect = efectoCostura
                        )
                    )
                }
            },
        enabled = habilitado,
        shape = RoundedCornerShape(10.dp),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevacionAnimada,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorContenedor,
            contentColor = colorContenido,
            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (icono != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = texto,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BotonPrincipalPreview() {
    TextilCircularTheme {
        BotonPrincipal(texto = "Guardar cambios", onClick = {})
    }
}
