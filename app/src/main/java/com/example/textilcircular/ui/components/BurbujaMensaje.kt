package com.example.textilcircular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Burbuja de chat para la mensajería interna (RF-20, RF-22).
 * Diferencia con claridad entre las consultas de clientes y las respuestas de la emprendedora.
 */
@Composable
fun BurbujaMensaje(
    mensaje: Mensaje,
    modifier: Modifier = Modifier
) {
    val esEmprendedora = mensaje.emisor == TipoEmisor.EMPRENDEDORA
    val alineacion = if (esEmprendedora) Alignment.CenterEnd else Alignment.CenterStart

    val fondoBurbuja = if (esEmprendedora) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val colorTexto = if (esEmprendedora) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = alineacion
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = fondoBurbuja,
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (esEmprendedora) 14.dp else 2.dp,
                        bottomEnd = if (esEmprendedora) 2.dp else 14.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = mensaje.contenido,
                style = MaterialTheme.typography.bodyLarge,
                color = colorTexto
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mensaje.fechaHora,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BurbujaMensajePreview() {
    TextilCircularTheme {
        Column {
            BurbujaMensaje(
                mensaje = Mensaje(
                    emisor = TipoEmisor.CLIENTE,
                    contenido = "¡Hola! ¿Este bolso de mezclilla resiste libros pesados?",
                    fechaHora = "10:30"
                )
            )
            BurbujaMensaje(
                mensaje = Mensaje(
                    emisor = TipoEmisor.EMPRENDEDORA,
                    contenido = "Hola, sí. Tiene costuras reforzadas con hilo de alta resistencia.",
                    fechaHora = "10:32"
                )
            )
        }
    }
}
