package com.example.textilcircular.ui.screens.chat

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BurbujaMensaje
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Pantalla 11: Conversación Interna (RF-20 a RF-22)
 * Chat seguro integrado en la plataforma que reemplaza el contacto por WhatsApp o Instagram.
 */
@Composable
fun ConversacionScreen(
    conversacion: Conversacion,
    mensajes: List<Mensaje>,
    onEnviarMensaje: (String) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textoRespuesta by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = conversacion.nombreCliente,
                onNavegarAtras = onNavegarAtras
            )
        },
        bottomBar = {
            // Caja inferior de respuesta rápida (RF-22)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
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
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                            .clip(RoundedCornerShape(24.dp))
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
            // Tarjeta superior del producto consultado (RF-21)
            conversacion.productoAsociado?.let { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
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
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Franja de seguridad de la plataforma (RF-20)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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
                    BurbujaMensaje(mensaje = mensaje)
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
