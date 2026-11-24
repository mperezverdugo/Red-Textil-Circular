package com.example.textilcircular.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaClaro

/**
 * ============================================================================
 * LOGO TEXTIL ANIMADO - IDENTIDAD "TEXTIL CIRCULAR"
 * ============================================================================
 * Isotipo animado de reciclaje textil que rota de manera suave y continua
 * (12 segundos por revolución completa) simbolizando el ciclo infinito y
 * sostenible de las fibras textiles y el suprarreciclaje artesanal.
 *
 * @param tamano Dimensión del contenedor circular (por defecto 32.dp).
 * @param animar Habilita o pausa la animación de rotación continua.
 * @param modifier Modificador de diseño para posicionamiento externo.
 */
@Composable
fun LogoTextilAnimado(
    tamano: Dp = 32.dp,
    animar: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Transición infinita para rotación continua de 360 grados en 12 segundos
    val anguloRotacion = if (animar) {
        val transicionInfinita = rememberInfiniteTransition(label = "transicionLogoTextil")
        val angulo by transicionInfinita.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "anguloRotacionLogo"
        )
        angulo
    } else {
        0f
    }

    // Contenedor circular con fondo suave TurquesaClaro
    Box(
        modifier = modifier
            .size(tamano)
            .background(color = TurquesaClaro, shape = CircleShape)
            .padding(tamano * 0.12f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_reciclaje_textil),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = anguloRotacion
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LogoTextilAnimadoPreview() {
    TextilCircularTheme {
        LogoTextilAnimado(tamano = 48.dp, animar = true)
    }
}
