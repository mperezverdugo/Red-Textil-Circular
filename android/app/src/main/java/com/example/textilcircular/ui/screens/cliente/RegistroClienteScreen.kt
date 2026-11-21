package com.example.textilcircular.ui.screens.cliente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.components.MedidorFortalezaClave
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla para que un cliente cree su cuenta e ingrese al mercado textil.
@Composable
fun RegistroClienteScreen(
    onNavegarAtras: () -> Unit,
    onRegistroCompletado: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistroClienteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistroClienteContent(
        uiState = uiState,
        onNombreCambiado = viewModel::onNombreCambiado,
        onCorreoCambiado = viewModel::onCorreoCambiado,
        onPasswordCambiado = viewModel::onPasswordCambiado,
        onAceptaLeyDatosCambiado = viewModel::onAceptaLeyDatosCambiado,
        onCrearCuentaClick = { viewModel.registrar(onExito = onRegistroCompletado) },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

// Estructura visual de los campos de registro de cliente.
@Composable
fun RegistroClienteContent(
    uiState: RegistroClienteUiState,
    onNombreCambiado: (String) -> Unit,
    onCorreoCambiado: (String) -> Unit,
    onPasswordCambiado: (String) -> Unit,
    onAceptaLeyDatosCambiado: (Boolean) -> Unit,
    onCrearCuentaClick: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.registro_cliente_titulo),
                onNavegarAtras = onNavegarAtras
            )
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.registro_cliente_bajada),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Aviso si ocurre algún problema general
            if (uiState.errorGeneral != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorGeneral,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Datos del cliente
            CampoTexto(
                valor = uiState.nombre,
                onValorCambiado = onNombreCambiado,
                etiqueta = stringResource(id = R.string.registro_cliente_campo_nombre),
                textoAyuda = stringResource(id = R.string.registro_cliente_campo_nombre_ayuda),
                mensajeError = uiState.errorNombre
            )

            CampoTexto(
                valor = uiState.correo,
                onValorCambiado = onCorreoCambiado,
                etiqueta = stringResource(id = R.string.registro_cliente_campo_correo),
                textoAyuda = stringResource(id = R.string.registro_cliente_campo_correo_ayuda),
                mensajeError = uiState.errorCorreo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                CampoTexto(
                    valor = uiState.password,
                    onValorCambiado = onPasswordCambiado,
                    etiqueta = stringResource(id = R.string.registro_cliente_campo_password),
                    textoAyuda = stringResource(id = R.string.registro_cliente_campo_password_ayuda),
                    mensajeError = uiState.errorPassword,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                MedidorFortalezaClave(clave = uiState.password)
            }

            // Aceptación de condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAceptaLeyDatosCambiado(!uiState.aceptaLeyDatos) }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = uiState.aceptaLeyDatos,
                    onCheckedChange = onAceptaLeyDatosCambiado,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.registro_cliente_ley_datos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BotonPrincipal(
                texto = stringResource(id = R.string.registro_cliente_boton_crear),
                onClick = onCrearCuentaClick,
                habilitado = !uiState.estaCargando && uiState.nombre.isNotBlank() && uiState.correo.isNotBlank() && uiState.password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroClienteScreenPreview() {
    TextilCircularTheme {
        RegistroClienteContent(
            uiState = RegistroClienteUiState(),
            onNombreCambiado = {},
            onCorreoCambiado = {},
            onPasswordCambiado = {},
            onAceptaLeyDatosCambiado = {},
            onCrearCuentaClick = {},
            onNavegarAtras = {}
        )
    }
}
