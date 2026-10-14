package com.example.textilcircular.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R

// Niveles de seguridad posibles para una contraseña.
enum class NivelFortaleza {
    VACIA,
    DEBIL,
    MEDIA,
    FUERTE
}

// Indicador visual de seguridad de la contraseña con barra segmentada de colores.
@Composable
fun MedidorFortalezaClave(
    clave: String,
    modifier: Modifier = Modifier
) {
    val fortaleza = calcularFortaleza(clave)
    if (fortaleza == NivelFortaleza.VACIA) return

    val (textoResId, segmentosActivos, colorBase) = when (fortaleza) {
        NivelFortaleza.DEBIL -> Triple(
            R.string.fortaleza_debil,
            1,
            MaterialTheme.colorScheme.error
        )
        NivelFortaleza.MEDIA -> Triple(
            R.string.fortaleza_media,
            2,
            MaterialTheme.colorScheme.tertiary
        )
        NivelFortaleza.FUERTE -> Triple(
            R.string.fortaleza_fuerte,
            3,
            MaterialTheme.colorScheme.primary
        )
        NivelFortaleza.VACIA -> Triple(
            R.string.fortaleza_debil,
            0,
            Color.Transparent
        )
    }

    val colorAnimado by animateColorAsState(targetValue = colorBase, label = "color_fortaleza")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { indice ->
                val activo = indice < segmentosActivos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (activo) colorAnimado else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(id = R.string.fortaleza_etiqueta, stringResource(id = textoResId)),
            style = MaterialTheme.typography.labelSmall,
            color = colorAnimado
        )
    }
}

// Evalúa la variedad de caracteres y la extensión de la contraseña ingresada.
fun calcularFortaleza(clave: String): NivelFortaleza {
    if (clave.isBlank()) return NivelFortaleza.VACIA
    if (clave.length < 6) return NivelFortaleza.DEBIL

    var puntos = 0
    if (clave.length >= 8) puntos++
    if (clave.any { it.isDigit() }) puntos++
    if (clave.any { it.isUpperCase() }) puntos++
    if (clave.any { !it.isLetterOrDigit() }) puntos++

    return when {
        puntos >= 3 -> NivelFortaleza.FUERTE
        puntos >= 1 -> NivelFortaleza.MEDIA
        else -> NivelFortaleza.DEBIL
    }
}
