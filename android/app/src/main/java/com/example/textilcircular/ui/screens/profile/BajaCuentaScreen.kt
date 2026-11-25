package com.example.textilcircular.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.theme.FormaBoton
import com.example.textilcircular.ui.theme.FormaTarjeta
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla para solicitar la baja voluntaria de la cuenta conectada a PerfilViewModel.
@Composable
fun BajaCuentaScreen(
    onConfirmarBaja: () -> Unit,
    onCancelarBaja: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    BajaCuentaContent(
        onConfirmarBaja = {
            viewModel.solicitarBajaVoluntaria(onExito = onConfirmarBaja)
        },
        onCancelarBaja = onCancelarBaja,
        modifier = modifier
    )
}

@Composable
fun BajaCuentaContent(
    onConfirmarBaja: () -> Unit,
    onCancelarBaja: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.baja_titulo),
                onNavegarAtras = onCancelarBaja
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onConfirmarBaja,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = FormaBoton,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.baja_boton_confirmar),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                BotonSecundario(
                    texto = stringResource(id = R.string.baja_boton_cancelar),
                    onClick = onCancelarBaja
                )
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                    .border(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.baja_explicacion_titulo),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = FormaTarjeta,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = stringResource(id = R.string.baja_explicacion_cuerpo),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BajaCuentaScreenPreview() {
    TextilCircularTheme {
        BajaCuentaContent(
            onConfirmarBaja = {},
            onCancelarBaja = {}
        )
    }
}
