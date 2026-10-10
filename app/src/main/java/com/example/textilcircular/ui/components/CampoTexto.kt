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
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Campo de texto reutilizable con etiqueta, texto de ayuda y aviso de error accesible.
 * Cumple con RNF-05 (accesibilidad y señalización doble: color + ícono).
 */
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
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                errorLabelColor = MaterialTheme.colorScheme.error
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
