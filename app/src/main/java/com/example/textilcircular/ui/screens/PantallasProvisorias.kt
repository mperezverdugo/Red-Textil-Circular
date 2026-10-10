package com.example.textilcircular.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.textilcircular.ui.components.BotonPrincipal

/**
 * Pantalla provisoria utilizada en el Bloque 2 de navegación.
 * Permite verificar las rutas, la interacción y el comportamiento de la pila de navegación
 * antes de implementar las pantallas definitivas en los bloques 3, 4, 5 y 6.
 */
@Composable
fun PantallaProvisoria(
    nombrePantalla: String,
    descripcion: String,
    textoAccion: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = nombrePantalla,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (textoAccion != null && onAccion != null) {
                Spacer(modifier = Modifier.height(24.dp))
                BotonPrincipal(
                    texto = textoAccion,
                    onClick = onAccion
                )
            }
        }
    }
}
