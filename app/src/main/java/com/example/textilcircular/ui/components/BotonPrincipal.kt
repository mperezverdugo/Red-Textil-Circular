package com.example.textilcircular.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * ====================================================================
 * BOTÓN PRINCIPAL - PARCHE REFORZADO TEXTIL
 * ====================================================================
 * Botón primario de acción de la aplicación (Turquesa institucional).
 *
 * Decisiones de diseño UI/UX explicadas paso a paso:
 * 1. Sensación táctil de tela acolchada: Al presionar el botón, se reduce sutilmente
 *    su escala a 0.97f mediante `animateFloatAsState` con efecto elástico (`spring`).
 *    Esto le transmite al usuario una respuesta física cálida y satisfactoria.
 * 2. Elevación viva: La elevación disminuye al presionar (como un botón que se hunde en tela)
 *    y se eleva en reposo, simulando una etiqueta o parche con relieve.
 * 3. Detalle de costura interna opcional (`conEfectoCostura`): Permite dibujar un
 *    pespunte interior de máquina de coser que refuerza la estética de confección.
 * 4. Soporte de ícono opcional: Agregar un ícono a la izquierda del texto ayuda mucho a
 *    personas con baja alfabetización digital (requisito de accesibilidad RNF-08).
 * 5. Altura táctil ergonómica: Altura mínima de 52dp, facilitando el toque incluso
 *    con una sola mano en movimiento o para adultos mayores.
 *
 * NOTA DE COMPATIBILIDAD:
 * Se respetan los 4 parámetros originales (texto, onClick, modifier, habilitado) para no
 * quebrar ninguna de las llamadas en las pantallas del sistema.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    // --- Nuevos parámetros opcionales de diseño textil ---
    icono: ImageVector? = null,
    conEfectoCostura: Boolean = false,
    animarPresion: Boolean = true,
    colorFondo: Color? = null,
    colorTexto: Color? = null
) {
    // Detectamos si el usuario está pulsando el botón
    val interactionSource = remember { MutableInteractionSource() }
    val estaPresionado by interactionSource.collectIsPressedAsState()

    // Micro-animación elástica de pulsación
    val escalaAnimada by animateFloatAsState(
        targetValue = if (animarPresion && estaPresionado && habilitado) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "EscalaBotonPrincipal"
    )

    // Elevación dinámica (se hunde levemente al presionar)
    val elevacionAnimada by animateDpAsState(
        targetValue = if (habilitado && estaPresionado) 1.dp else 3.dp,
        label = "ElevacionBotonPrincipal"
    )

    // Medidas para el pespunte de costura interno
    val grosorPespuntePx = with(LocalDensity.current) { 1.5.dp.toPx() }
    val largoPuntadaPx = with(LocalDensity.current) { 5.dp.toPx() }
    val separacionPuntadaPx = with(LocalDensity.current) { 3.dp.toPx() }
    val radioEsquinaPx = with(LocalDensity.current) { 10.dp.toPx() }
    val margenInternoPx = with(LocalDensity.current) { 4.dp.toPx() }

    val efectoPespunte = remember(largoPuntadaPx, separacionPuntadaPx) {
        PathEffect.dashPathEffect(floatArrayOf(largoPuntadaPx, separacionPuntadaPx), 0f)
    }

    // Colores efectivos con fallback a la paleta central
    val contenedorEfectivo = colorFondo ?: MaterialTheme.colorScheme.primary
    val contenidoEfectivo = colorTexto ?: MaterialTheme.colorScheme.onPrimary

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = escalaAnimada
                scaleY = escalaAnimada
            }
            .drawBehind {
                // Dibuja el pespunte perimetral interno si está activado
                if (conEfectoCostura && habilitado) {
                    drawRoundRect(
                        color = contenidoEfectivo.copy(alpha = 0.5f),
                        topLeft = Offset(margenInternoPx, margenInternoPx),
                        size = Size(
                            size.width - (margenInternoPx * 2),
                            size.height - (margenInternoPx * 2)
                        ),
                        cornerRadius = CornerRadius(radioEsquinaPx, radioEsquinaPx),
                        style = Stroke(
                            width = grosorPespuntePx,
                            pathEffect = efectoPespunte
                        )
                    )
                }
            },
        enabled = habilitado,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevacionAnimada,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = contenedorEfectivo,
            contentColor = contenidoEfectivo,
            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (habilitado) contenidoEfectivo else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
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
        BotonPrincipal(
            texto = "Guardar cambios",
            conEfectoCostura = true,
            onClick = {}
        )
    }
}
