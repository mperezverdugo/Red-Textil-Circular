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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.ui.theme.AzulRevision
import com.example.textilcircular.ui.theme.AzulRevisionFondo
import com.example.textilcircular.ui.theme.GrisBaja
import com.example.textilcircular.ui.theme.GrisBajaFondo
import com.example.textilcircular.ui.theme.RojoError
import com.example.textilcircular.ui.theme.RojoErrorFondo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.VerdeExito
import com.example.textilcircular.ui.theme.VerdeExitoFondo

// Etiqueta visual con color e ícono que muestra el estado de una ficha.
@Composable
fun InsigniaEstado(
    estado: EstadoFichaEmprendimiento,
    modifier: Modifier = Modifier
) {
    val (colorFondo, colorContenido, icono, textoResId) = when (estado) {
        EstadoFichaEmprendimiento.ACTIVA -> Quadruple(
            VerdeExitoFondo,
            VerdeExito,
            Icons.Default.Check,
            R.string.ficha_estado_activa
        )
        EstadoFichaEmprendimiento.OCULTA -> Quadruple(
            GrisBajaFondo,
            GrisBaja,
            Icons.Default.Info,
            R.string.ficha_estado_oculta
        )
        EstadoFichaEmprendimiento.POR_REVISAR -> Quadruple(
            AzulRevisionFondo,
            AzulRevision,
            Icons.Default.Warning,
            R.string.ficha_estado_por_revisar
        )
        EstadoFichaEmprendimiento.DADA_DE_BAJA -> Quadruple(
            RojoErrorFondo,
            RojoError,
            Icons.Default.Close,
            R.string.ficha_estado_dada_de_baja
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = colorFondo, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = colorContenido, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorContenido,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(id = textoResId),
            style = MaterialTheme.typography.labelSmall,
            color = colorContenido
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Preview(showBackground = true)
@Composable
fun InsigniaEstadoPreview() {
    TextilCircularTheme {
        InsigniaEstado(estado = EstadoFichaEmprendimiento.ACTIVA)
    }
}
