package com.example.textilcircular.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaFundacion
import com.example.textilcircular.util.ResolvedorImagenProducto
import java.io.File

// Tarjeta que muestra la foto, nombre y descripción corta de un producto en el catálogo
// Siguiendo el estilo editorial de moda con muestras visuales (Opción A).
@Composable
fun TarjetaProducto(
    producto: Producto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarInsigniaPendiente: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column {
            val primeraFoto = producto.fotos.firstOrNull()
            val bitmap = remember(primeraFoto) {
                if (!primeraFoto.isNullOrBlank()) {
                    val archivo = File(primeraFoto)
                    if (archivo.exists()) {
                        BitmapFactory.decodeFile(archivo.absolutePath)
                    } else null
                } else null
            }

            // Imagen del producto: foto real local o muestra visual vectorial (Opción A)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val drawableMuestra = remember(producto) {
                        ResolvedorImagenProducto.resolverDrawable(producto, 0)
                    }

                    Image(
                        painter = painterResource(id = drawableMuestra),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Botón decorativo de favoritos/estilo en la esquina superior derecha (estilo referencia)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = TurquesaFundacion,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (mostrarInsigniaPendiente && producto.pendienteSincronizar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        InsigniaPendiente()
                    }
                }
            }

            // Datos del producto con jerarquía editorial
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Upcycling Textil",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmarilloAcento,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.descripcionCorta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaProductoPreview() {
    TextilCircularTheme {
        TarjetaProducto(
            producto = Producto(
                nombre = "Bolso Upcycling Mezclilla",
                descripcionCorta = "Confeccionado a partir de jeans recuperados de la comunidad.",
                pendienteSincronizar = true
            ),
            onClick = {}
        )
    }
}
