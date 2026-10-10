package com.example.textilcircular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.NaranjaAdvertencia
import com.example.textilcircular.ui.theme.NaranjaAdvertenciaFondo
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Insignia visual para elementos creados o editados offline pendientes de sincronización (RF-35).
 * Combina ícono y texto para cumplir con la regla de accesibilidad (RNF-05).
 */
@Composable
fun InsigniaPendiente(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = NaranjaAdvertenciaFondo, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = NaranjaAdvertencia, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = NaranjaAdvertencia,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(id = R.string.estado_pendiente_subir),
            style = MaterialTheme.typography.labelSmall,
            color = NaranjaAdvertencia
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InsigniaPendientePreview() {
    TextilCircularTheme {
        InsigniaPendiente()
    }
}
