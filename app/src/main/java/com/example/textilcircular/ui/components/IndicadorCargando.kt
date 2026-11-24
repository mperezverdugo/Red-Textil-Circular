package com.example.textilcircular.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaFundacion

/**
 * Indicador de carga accesible con texto descriptivo (RNF-03, RNF-04).
 * Rediseñado con la animación artesanal de "Costura en Movimiento":
 * - Pespunte circular con fase continua que simula una aguja e hilo cosiendo en bucle.
 * - Retazo textil central con pulso elástico suave (0.95f a 1.05f).
 */
@Composable
fun IndicadorCargando(
    mensaje: String = stringResource(id = R.string.estado_cargando),
    modifier: Modifier = Modifier
) {
    val transicionInfinita = rememberInfiniteTransition(label = "transicionCargaTextil")

    // Animación de fase para el efecto de costura en movimiento circular
    val faseCostura by transicionInfinita.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "faseCosturaCircular"
    )

    // Animación de pulso elástico para el retazo central
    val escalaRetazo by transicionInfinita.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaRetazoCentral"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Contenedor visual de la costura en movimiento y retazo
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthPx = 3.dp.toPx()
                val dashPx = 8.dp.toPx()
                val gapPx = 6.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), faseCostura)
                val radio = (size.minDimension - strokeWidthPx) / 2f

                drawCircle(
                    color = TurquesaFundacion,
                    radius = radio,
                    center = center,
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = pathEffect,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Retazo textil central con pulso respiratorio suave
            Image(
                painter = painterResource(id = R.drawable.ic_textil_retazo),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        scaleX = escalaRetazo
                        scaleY = escalaRetazo
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun IndicadorCargandoPreview() {
    TextilCircularTheme {
        IndicadorCargando(mensaje = "Preparando creaciones textiles…")
    }
}
