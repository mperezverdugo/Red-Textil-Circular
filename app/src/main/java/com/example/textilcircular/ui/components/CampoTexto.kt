package com.example.textilcircular.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.BlancoLino
import com.example.textilcircular.ui.theme.GrisBorde
import com.example.textilcircular.ui.theme.GrisTextoSecundario
import com.example.textilcircular.ui.theme.LinoContenedor
import com.example.textilcircular.ui.theme.NegroTexto
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaFundacion

/**
 * Campo de texto reutilizable con etiqueta, texto de ayuda y aviso de error accesible.
 * Estética Textil:
 * - Fondo cálido en fibra natural (BlancoLino #FAF8F5).
 * - Borde en estado normal tipo hilo de hilván (GrisBorde #D6CFC3).
 * - Borde enfocado en Turquesa institucional con esquinas de 12.dp.
 * - Doble señalización de accesibilidad (color + ícono) según RNF-05.
 *
 * Mantiene intactos todos los parámetros de la firma original.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoTexto(
    valor: String,
    onValorCambiado: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    textoAyuda: String? = null,
    mensajeError: String? = null,
    esError: Boolean = mensajeError != null,
    esSoloLectura: Boolean = false,
    lineasMaximas: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    iconoInicio: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambiado,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = etiqueta, style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = iconoInicio,
            trailingIcon = if (esError) {
                {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(id = R.string.cd_icono_error),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            isError = esError,
            readOnly = esSoloLectura,
            maxLines = lineasMaximas,
            minLines = if (lineasMaximas > 1) 3 else 1,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BlancoLino,
                unfocusedContainerColor = BlancoLino,
                disabledContainerColor = LinoContenedor,
                errorContainerColor = BlancoLino,
                focusedBorderColor = TurquesaFundacion,
                unfocusedBorderColor = GrisBorde,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = TurquesaFundacion,
                unfocusedLabelColor = GrisTextoSecundario,
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedTextColor = NegroTexto,
                unfocusedTextColor = NegroTexto
            )
        )

        if (esError && !mensajeError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        } else if (!textoAyuda.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = textoAyuda,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CampoTextoPreview() {
    TextilCircularTheme {
        CampoTexto(
            valor = "Taller Telar",
            onValorCambiado = {},
            etiqueta = "Nombre comercial",
            textoAyuda = "Ejemplo: Taller Hilos del Sur"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CampoTextoErrorPreview() {
    TextilCircularTheme {
        CampoTexto(
            valor = "12345678-k",
            onValorCambiado = {},
            etiqueta = "RUT",
            mensajeError = "Este RUT ya tiene una cuenta registrada"
        )
    }
}
