package com.example.textilcircular.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla de inicio de sesión para emprendedora y administradora conectada a LoginViewModel.
@Composable
fun LoginScreen(
    onNavegarAtras: () -> Unit,
    onLoginExitosoEmprendedora: () -> Unit,
    onLoginExitosoAdmin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        onCorreoCambiado = viewModel::onCorreoCambiado,
        onPasswordCambiado = viewModel::onPasswordCambiado,
        onIngresarClick = {
            viewModel.iniciarSesion(
                onExitoEmprendedora = onLoginExitosoEmprendedora,
                onExitoAdmin = onLoginExitosoAdmin
            )
        },
        onDemoEmprendedoraClick = {
            viewModel.iniciarSesionDemoEmprendedora(onExito = onLoginExitosoEmprendedora)
        },
        onDemoAdminClick = {
            viewModel.iniciarSesionDemoAdmin(onExito = onLoginExitosoAdmin)
        },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambiado: (String) -> Unit,
    onPasswordCambiado: (String) -> Unit,
    onIngresarClick: () -> Unit,
    onDemoEmprendedoraClick: () -> Unit,
    onDemoAdminClick: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.login_titulo),
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
                text = stringResource(id = R.string.login_bajada),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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

            CampoTexto(
                valor = uiState.correo,
                onValorCambiado = onCorreoCambiado,
                etiqueta = stringResource(id = R.string.login_campo_correo),
                mensajeError = uiState.errorCorreo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            CampoTexto(
                valor = uiState.password,
                onValorCambiado = onPasswordCambiado,
                etiqueta = stringResource(id = R.string.login_campo_password),
                mensajeError = uiState.errorPassword,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            BotonPrincipal(
                texto = stringResource(id = R.string.login_boton_ingresar),
                onClick = onIngresarClick,
                habilitado = !uiState.estaCargando && uiState.correo.isNotBlank() && uiState.password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Accesos directos para demostración
            Text(
                text = stringResource(id = R.string.login_seccion_demo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            BotonSecundario(
                texto = stringResource(id = R.string.login_boton_demo_emprendedora),
                onClick = onDemoEmprendedoraClick,
                habilitado = !uiState.estaCargando
            )

            BotonSecundario(
                texto = stringResource(id = R.string.login_boton_demo_admin),
                onClick = onDemoAdminClick,
                habilitado = !uiState.estaCargando
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TextilCircularTheme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoCambiado = {},
            onPasswordCambiado = {},
            onIngresarClick = {},
            onDemoEmprendedoraClick = {},
            onDemoAdminClick = {},
            onNavegarAtras = {}
        )
    }
}
