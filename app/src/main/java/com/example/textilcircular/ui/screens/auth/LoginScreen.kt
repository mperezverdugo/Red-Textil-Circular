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
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Pantalla 2: Iniciar Sesión (RF-08)
 * Permite acceder a la cuenta de la emprendedora o al rol administrador.
 */
@Composable
fun LoginScreen(
    onNavegarAtras: () -> Unit,
    onIngresarClick: (correo: String, password: String) -> Unit,
    onDemoEmprendedoraClick: () -> Unit,
    onDemoAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

            CampoTexto(
                valor = correo,
                onValorCambiado = { correo = it },
                etiqueta = stringResource(id = R.string.login_campo_correo),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            CampoTexto(
                valor = password,
                onValorCambiado = { password = it },
                etiqueta = stringResource(id = R.string.login_campo_password),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // TODO(Capa 2): La validación de credenciales reales y tokens se conectará con el ViewModel y AuthRepository.
            BotonPrincipal(
                texto = stringResource(id = R.string.login_boton_ingresar),
                onClick = { onIngresarClick(correo, password) },
                habilitado = correo.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Sección de demostración para evaluación docente y pruebas (DSY1105)
            Text(
                text = stringResource(id = R.string.login_seccion_demo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // TODO(Capa 2): El rol vendrá de la autenticación en el servidor.
            BotonSecundario(
                texto = stringResource(id = R.string.login_boton_demo_emprendedora),
                onClick = onDemoEmprendedoraClick
            )

            BotonSecundario(
                texto = stringResource(id = R.string.login_boton_demo_admin),
                onClick = onDemoAdminClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TextilCircularTheme {
        LoginScreen(
            onNavegarAtras = {},
            onIngresarClick = { _, _ -> },
            onDemoEmprendedoraClick = {},
            onDemoAdminClick = {}
        )
    }
}
