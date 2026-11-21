package com.example.textilcircular.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.EstadoVacio
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Bandeja de mensajes con la lista de conversaciones de clientes o de la emprendedora.
@Composable
fun MensajesScreen(
    conversaciones: List<Conversacion>,
    onConversacionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    esCliente: Boolean = false,
    onExplorarClick: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.mensajes_titulo)
            )
        },
        modifier = modifier
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            if (conversaciones.isEmpty()) {
                if (esCliente) {
                    EstadoVacio(
                        titulo = stringResource(id = R.string.mensajes_cliente_vacio_titulo),
                        mensaje = stringResource(id = R.string.mensajes_cliente_vacio_mensaje),
                        textoAccion = stringResource(id = R.string.mensajes_cliente_vacio_accion),
                        onAccionClick = onExplorarClick,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    EstadoVacio(
                        titulo = stringResource(id = R.string.mensajes_vacio_titulo),
                        mensaje = stringResource(id = R.string.mensajes_vacio_mensaje),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (esCliente) stringResource(id = R.string.mensajes_cliente_bajada) else stringResource(id = R.string.mensajes_bajada),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(conversaciones, key = { it.id }) { conversacion ->
                            FilaConversacion(
                                conversacion = conversacion,
                                onClick = { onConversacionClick(conversacion.id) },
                                esCliente = esCliente
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaConversacion(
    conversacion: Conversacion,
    onClick: () -> Unit,
    esCliente: Boolean = false
) {
    val nombre = if (esCliente) conversacion.nombreEmprendimiento.ifBlank { stringResource(id = R.string.mercado_taller_textil) } else conversacion.nombreCliente
    val noLeidos = if (esCliente) conversacion.noLeidosCliente else conversacion.noLeidosEmprendedora

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (esCliente) Icons.Default.ShoppingCart else Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = conversacion.fechaHora,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversacion.ultimoMensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (noLeidos > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (noLeidos > 0) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (noLeidos > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = noLeidos.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MensajesScreenPreview() {
    TextilCircularTheme {
        MensajesScreen(
            conversaciones = DatosDemo.ConversacionesDemo,
            onConversacionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MensajesScreenVacioPreview() {
    TextilCircularTheme {
        MensajesScreen(
            conversaciones = emptyList(),
            onConversacionClick = {}
        )
    }
}
