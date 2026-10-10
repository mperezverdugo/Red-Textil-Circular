package com.example.textilcircular.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Diálogo de confirmación accesible para acciones sensibles o destructivas (RF-11, RF-13, RF-28).
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    textoCancelar: String = stringResource(id = R.string.accion_cancelar),
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    esAccionPeligrosa: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (esAccionPeligrosa) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = textoConfirmar,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(
                    text = textoCancelar,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DialogoConfirmacionPreview() {
    TextilCircularTheme {
        DialogoConfirmacion(
            titulo = "¿Eliminar producto?",
            mensaje = "Esta prenda se eliminará de tu catálogo y ya no aparecerá en el directorio web.",
            textoConfirmar = "Sí, eliminar",
            esAccionPeligrosa = true,
            onConfirmar = {},
            onCancelar = {}
        )
    }
}
