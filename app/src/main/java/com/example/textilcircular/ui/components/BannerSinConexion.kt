package com.example.textilcircular.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
 * Banner informativo para el modo sin conexión (RNF-02, RF-35).
 * Notifica a la usuaria de forma tranquila y transparente que puede seguir trabajando.
 */
@Composable
fun BannerSinConexion(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .background(NaranjaAdvertenciaFondo)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(id = R.string.cd_icono_alerta),
                tint = NaranjaAdvertencia,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(id = R.string.estado_sin_conexion),
                style = MaterialTheme.typography.bodyMedium,
                color = NaranjaAdvertencia
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BannerSinConexionPreview() {
    TextilCircularTheme {
        BannerSinConexion(visible = true)
    }
}
