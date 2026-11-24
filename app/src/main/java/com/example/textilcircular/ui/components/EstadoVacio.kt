package com.example.textilcircular.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.GrisBorde
import com.example.textilcircular.ui.theme.LinoContenedor
import com.example.textilcircular.ui.theme.NegroTexto
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Componente para representar estados vacíos con mensaje amigable y acción directa.
 * Cumple con RNF-04 (lenguaje motivador y claro) e incorpora:
 * - Retazo textil (`ic_textil_retazo`) dentro de un círculo en `LinoContenedor`.
 * - Borde circular punteado simulando hilván artesanal.
 * - Tipografía cálida y jerarquizada.
 *
 * Mantiene intactos todos los parámetros originales de la firma.
 */
@Composable
fun EstadoVacio(
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    icono: ImageVector = Icons.Default.Info,
    textoAccion: String? = null,
    onAccionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Círculo contenedor en LinoContenedor con borde hilvanado punteado
        Box(
            modifier = Modifier
                .size(96.dp)
                .drawWithContent {
                    val strokeWidthPx = 1.5.dp.toPx()
                    val puntadaPx = 6.dp.toPx()
                    val separacionPx = 4.dp.toPx()
                    val radioFondo = size.minDimension / 2f
                    val radioBorde = (size.minDimension - strokeWidthPx) / 2f

                    // Fondo circular en tono lino crudo contenedor
                    drawCircle(
                        color = LinoContenedor,
                        radius = radioFondo,
                        center = center
                    )

                    // Pespunte punteado artesanal
                    drawCircle(
                        color = GrisBorde,
                        radius = radioBorde,
                        center = center,
                        style = Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(puntadaPx, separacionPx), 0f)
                        )
                    )

                    drawContent()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_textil_retazo),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NegroTexto,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (textoAccion != null && onAccionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            BotonPrincipal(
                texto = textoAccion,
                onClick = onAccionClick,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EstadoVacioPreview() {
    TextilCircularTheme {
        EstadoVacio(
            titulo = "Tu catálogo está esperando",
            mensaje = "Sube tus primeras creaciones textiles para que la comunidad pueda encontrarte.",
            textoAccion = "Agregar mi primer producto",
            onAccionClick = {}
        )
    }
}
