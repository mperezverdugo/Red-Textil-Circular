package com.example.textilcircular.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Indicador que muestra en qué paso del formulario va la persona.
@Composable
fun BarraProgresoPasos(
    pasoActual: Int,
    totalPasos: Int,
    tituloPaso: String,
    modifier: Modifier = Modifier
) {
    val progreso = (pasoActual.toFloat() / totalPasos.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.paso_indicador, pasoActual, totalPasos),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tituloPaso,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(FormaPildora),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BarraProgresoPasosPreview() {
    TextilCircularTheme {
        BarraProgresoPasos(
            pasoActual = 2,
            totalPasos = 4,
            tituloPaso = "Tu emprendimiento"
        )
    }
}
