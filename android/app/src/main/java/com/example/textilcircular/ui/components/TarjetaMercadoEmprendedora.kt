package com.example.textilcircular.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.ItemMercadoEmprendedora
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.util.ResolvedorImagenProducto
import java.io.File

// Tarjeta que presenta a una emprendedora en el mercado para los clientes.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaMercadoEmprendedora(
    item: ItemMercadoEmprendedora,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Column {
            val foto = item.fotoPortada
            val bitmap = remember(foto) {
                if (!foto.isNullOrBlank()) {
                    val archivo = File(foto)
                    if (archivo.exists()) {
                        BitmapFactory.decodeFile(archivo.absolutePath)
                    } else null
                } else null
            }

            // Portada con la primera foto de su catálogo o un recuadro de reserva
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = item.emprendedora.nombreComercial,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val drawableMuestra = remember(item.emprendedora.id) {
                        ResolvedorImagenProducto.resolverDrawablePorEmprendedora(item.emprendedora.id)
                    }
                    Image(
                        painter = painterResource(id = drawableMuestra),
                        contentDescription = item.emprendedora.nombreComercial,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Información visible para el cliente: nombre comercial, rubros y ubicación
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.emprendedora.nombreComercial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Ubicación (comuna y región)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.emprendedora.comuna}, ${item.emprendedora.region}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rubros textiles destacados
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.emprendedora.rubros.take(3).forEach { rubro ->
                        AssistChip(
                            onClick = {},
                            shape = FormaPildora,
                            label = {
                                Text(
                                    text = rubro.nombre,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
}
