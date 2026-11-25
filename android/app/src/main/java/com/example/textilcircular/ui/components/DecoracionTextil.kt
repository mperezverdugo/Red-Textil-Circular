package com.example.textilcircular.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.TurquesaFundacion

/**
 * Modificador de Compose que dibuja una sutil trama geométrica minimalista inspirada
 * en el tejido de telar y fibras upcycling de la Fundación Textil Circular.
 */
fun Modifier.fondoTramaTextil(
    colorLinea: Color = TurquesaFundacion,
    colorAcento: Color = AmarilloAcento,
    espaciado: Dp = 22.dp,
    alpha: Float = 0.04f
): Modifier = this.drawBehind {
    val step = espaciado.toPx()
    val strokeWidth = 1.2f
    val w = size.width
    val h = size.height

    if (w <= 0f || h <= 0f) return@drawBehind

    // Hilos diagonales de urdimbre (hacia 45°)
    var x = -h
    var index = 0
    while (x < w + h) {
        val color = if (index % 5 == 0) {
            colorAcento.copy(alpha = alpha * 1.1f)
        } else {
            colorLinea.copy(alpha = alpha)
        }
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x + h, h),
            strokeWidth = strokeWidth
        )
        x += step
        index++
    }

    // Hilos de trama transversal (hacia 135°, con espaciado para ligereza visual)
    var y = -w
    var indexY = 0
    val stepY = step * 1.5f
    while (y < h + w) {
        if (indexY % 2 == 0) {
            drawLine(
                color = colorLinea.copy(alpha = alpha * 0.6f),
                start = Offset(0f, y + w),
                end = Offset(w, y),
                strokeWidth = strokeWidth
            )
        }
        y += stepY
        indexY++
    }
}

/**
 * Modificador que dibuja un borde de pespunte textil artesanal punteado.
 */
fun Modifier.bordePespunteTextil(
    color: Color = TurquesaFundacion.copy(alpha = 0.2f),
    grosor: Dp = 1.dp
): Modifier = this.drawBehind {
    val y = size.height - (grosor.toPx() / 2)
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = grosor.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
    )
}
