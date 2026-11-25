package com.example.textilcircular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Rol
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.TurquesaFundacion

/**
 * Avatar circular estilizado para usuarios, artesanas y administradoras (Opción B: aros decorativos).
 * Diferencia con precisión el rol del usuario utilizando vectores Material específicos y colores institucionales.
 */
@Composable
fun AvatarUsuario(
    rol: Rol,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp
) {
    val (drawableRes, colorBorde, colorFondo) = when (rol) {
        Rol.EMPRENDEDORA -> Triple(
            R.drawable.ic_avatar_taller,
            TurquesaFundacion.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        )
        Rol.CLIENTE -> Triple(
            R.drawable.ic_avatar_cliente,
            AmarilloAcento.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        )
        Rol.ADMINISTRADORA -> Triple(
            R.drawable.ic_avatar_admin,
            TurquesaFundacion.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        )
    }

    AvatarCircularContenedor(
        drawableRes = drawableRes,
        tamano = tamano,
        colorBorde = colorBorde,
        colorFondo = colorFondo,
        modifier = modifier
    )
}

/**
 * Variante con recurso vectorial explícito para compatibilidad con vistas de chat y perfiles existentes.
 */
@Composable
fun AvatarUsuario(
    drawableRes: Int,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp,
    colorBorde: Color = TurquesaFundacion.copy(alpha = 0.3f),
    colorFondo: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
) {
    AvatarCircularContenedor(
        drawableRes = drawableRes,
        tamano = tamano,
        colorBorde = colorBorde,
        colorFondo = colorFondo,
        modifier = modifier
    )
}

@Composable
private fun AvatarCircularContenedor(
    drawableRes: Int,
    tamano: Dp,
    colorBorde: Color,
    colorFondo: Color,
    modifier: Modifier = Modifier
) {
    val grosorAro = if (tamano >= 64.dp) 2.dp else 1.5.dp
    val paddingInterno = if (tamano >= 64.dp) 4.dp else 2.dp
    val iconoTamano = tamano * 0.56f

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .border(width = grosorAro, color = colorBorde, shape = CircleShape)
            .padding(paddingInterno)
            .clip(CircleShape)
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            tint = Color.Unspecified, // Permite que los vectores bicromáticos mantengan sus colores institucionales
            modifier = Modifier.size(iconoTamano)
        )
    }
}
