package com.example.textilcircular.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Menú desplegable para elegir de una lista cerrada sin escribir a mano.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegable(
    opciones: List<String>,
    seleccionado: String,
    onOpcionSeleccionada: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    textoAyuda: String? = null,
    mensajeError: String? = null
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expandido && habilitado,
            onExpandedChange = { if (habilitado) expandido = it }
        ) {
            OutlinedTextField(
                value = if (seleccionado.isNotBlank()) seleccionado else stringResource(id = R.string.selector_seleccionar),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = etiqueta, style = MaterialTheme.typography.bodyLarge) },
                trailingIcon = {
                    if (habilitado) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                    }
                },
                enabled = habilitado,
                isError = mensajeError != null,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandido && habilitado,
                onDismissRequest = { expandido = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = opcion,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onOpcionSeleccionada(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }

        if (mensajeError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else if (!habilitado && textoAyuda == null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.selector_bloqueado_ayuda),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
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
fun SelectorDesplegablePreview() {
    TextilCircularTheme {
        SelectorDesplegable(
            opciones = listOf("Valparaíso", "Metropolitana de Santiago", "Biobío"),
            seleccionado = "Valparaíso",
            onOpcionSeleccionada = {},
            etiqueta = "Región"
        )
    }
}
