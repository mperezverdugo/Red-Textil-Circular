package com.example.textilcircular.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
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

// Burbuja de chat que cambia de lado y color según quién escribió el mensaje.
@Composable
fun BurbujaMensaje(
    mensaje: Mensaje,
    modifier: Modifier = Modifier,
    esEmisorPropio: Boolean = mensaje.emisor == TipoEmisor.EMPRENDEDORA
) {
    val alineacion = if (esEmisorPropio) Alignment.CenterEnd else Alignment.CenterStart

    val fondoBurbuja = if (esEmisorPropio) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val colorTexto = if (esEmisorPropio) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val formaBurbuja = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (esEmisorPropio) 18.dp else 4.dp,
        bottomEnd = if (esEmisorPropio) 4.dp else 18.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = alineacion
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = fondoBurbuja,
                    shape = formaBurbuja
                )
                .then(
                    if (!esEmisorPropio) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            shape = formaBurbuja
                        )
                    } else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!mensaje.nombreProducto.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mensaje.nombreProducto,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

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
