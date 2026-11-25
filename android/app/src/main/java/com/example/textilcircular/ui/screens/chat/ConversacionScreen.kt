package com.example.textilcircular.ui.screens.chat

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BurbujaMensaje
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.FormaCampo
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.util.ResolvedorImagenProducto
import java.io.File

// Pantalla de chat entre la emprendedora y un cliente interesado.
@Composable
fun ConversacionScreen(
    conversacion: Conversacion,
    mensajes: List<Mensaje>,
    onEnviarMensaje: (String) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier,
    esCliente: Boolean = false,
    mensajeSugerido: String? = null,
    productoConsultado: Producto? = null,
    onSimularInteraccion: (() -> Unit)? = null,
    textoSimularInteraccion: String? = null,
    onVerPerfilEmprendedoraClick: (() -> Unit)? = null
) {
    var textoRespuesta by remember(conversacion.id, mensajeSugerido) {
        mutableStateOf(mensajeSugerido ?: "")
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (esCliente) conversacion.nombreEmprendimiento.ifBlank { stringResource(id = R.string.mercado_taller_textil) } else conversacion.nombreCliente,
                subtitulo = if (esCliente && onVerPerfilEmprendedoraClick != null) stringResource(id = R.string.chat_subtitulo_tocar_perfil) else null,
                onNavegarAtras = onNavegarAtras,
                onTituloClick = if (esCliente) onVerPerfilEmprendedoraClick else null
            )
        },
        bottomBar = {
            // Barra de entrada y envío de mensajes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (onSimularInteraccion != null && textoSimularInteraccion != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AssistChip(
                            onClick = onSimularInteraccion,
                            label = {
                                Text(
                                    text = textoSimularInteraccion,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textoRespuesta,
                        onValueChange = { textoRespuesta = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.chat_campo_placeholder),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = FormaPildora,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textoRespuesta.isNotBlank()) {
                                onEnviarMensaje(textoRespuesta)
                                textoRespuesta = ""
                            }
                        },
                        enabled = textoRespuesta.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (textoRespuesta.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(id = R.string.chat_boton_enviar),
                            tint = if (textoRespuesta.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            // Producto consultado en esta conversación
            val productoAMostrar = conversacion.productoAsociado ?: productoConsultado
            productoAMostrar?.let { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = FormaTarjeta,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val primeraFoto = producto.fotos.firstOrNull()
                        val bitmap = remember(primeraFoto) {
                            if (!primeraFoto.isNullOrBlank()) {
                                val archivo = File(primeraFoto)
                                if (archivo.exists()) BitmapFactory.decodeFile(archivo.absolutePath) else null
                            } else null
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(FormaCampo)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
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
                                val muestra = remember(producto) {
                                    ResolvedorImagenProducto.resolverDrawable(producto, 0)
                                }
                                Image(
                                    painter = painterResource(id = muestra),
                                    contentDescription = producto.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stringResource(id = R.string.chat_producto_consultado_etiqueta),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Recordatorio de seguridad de la plataforma
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = FormaCampo)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.chat_aviso_seguridad),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lista de mensajes en la conversación
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(mensajes, key = { it.id }) { mensaje ->
                    BurbujaMensaje(
                        mensaje = mensaje,
                        esEmisorPropio = if (esCliente) mensaje.emisor == TipoEmisor.CLIENTE else mensaje.emisor == TipoEmisor.EMPRENDEDORA
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversacionScreenPreview() {
    TextilCircularTheme {
        ConversacionScreen(
            conversacion = DatosDemo.ConversacionesDemo.first(),
            mensajes = DatosDemo.MensajesConversacionDemo,
            onEnviarMensaje = {},
            onNavegarAtras = {}
        )
    }
}
