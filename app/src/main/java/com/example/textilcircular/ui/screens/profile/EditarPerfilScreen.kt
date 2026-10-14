package com.example.textilcircular.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.components.ChipsRubros
import com.example.textilcircular.ui.components.SelectorDesplegable
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla para modificar los datos personales y de contacto conectada a PerfilViewModel.
@Composable
fun EditarPerfilScreen(
    onGuardarExitoso: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditarPerfilContent(
        uiState = uiState,
        onNombreCambiado = viewModel::onEditNombreCambiado,
        onNombreComercialCambiado = viewModel::onEditNombreComercialCambiado,
        onRegionSeleccionada = viewModel::onEditRegionSeleccionada,
        onComunaSeleccionada = viewModel::onEditComunaSeleccionada,
        onRubroToggled = viewModel::onEditRubroToggled,
        onInstagramCambiado = viewModel::onEditInstagramCambiado,
        onGuardarCambios = {
            viewModel.guardarCambiosPerfil(onExito = onGuardarExitoso)
        },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

@Composable
fun EditarPerfilContent(
    uiState: PerfilUiState,
    onNombreCambiado: (String) -> Unit,
    onNombreComercialCambiado: (String) -> Unit,
    onRegionSeleccionada: (String) -> Unit,
    onComunaSeleccionada: (String) -> Unit,
    onRubroToggled: (Rubro) -> Unit,
    onInstagramCambiado: (String) -> Unit,
    onGuardarCambios: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comunasDisponibles = remember(uiState.editRegion) {
        if (uiState.editRegion.isNotBlank()) {
            DatosDemo.RegionesConComunas[uiState.editRegion] ?: emptyList()
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.editar_perfil_titulo),
                onNavegarAtras = onNavegarAtras
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                BotonPrincipal(
                    texto = stringResource(id = R.string.accion_guardar),
                    onClick = onGuardarCambios,
                    habilitado = uiState.editNombre.isNotBlank() &&
                            uiState.editNombreComercial.isNotBlank() &&
                            uiState.editRegion.isNotBlank() &&
                            uiState.editComuna.isNotBlank()
                )
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CampoTexto(
                valor = uiState.editNombre,
                onValorCambiado = onNombreCambiado,
                etiqueta = stringResource(id = R.string.registro_campo_nombre),
                textoAyuda = stringResource(id = R.string.registro_campo_nombre_ayuda),
                mensajeError = uiState.errorNombre
            )

            CampoTexto(
                valor = uiState.editNombreComercial,
                onValorCambiado = onNombreComercialCambiado,
                etiqueta = stringResource(id = R.string.registro_campo_nombre_comercial),
                textoAyuda = stringResource(id = R.string.registro_campo_nombre_comercial_ayuda),
                mensajeError = uiState.errorNombreComercial
            )

            SelectorDesplegable(
                opciones = DatosDemo.ListaRegiones,
                seleccionado = uiState.editRegion,
                onOpcionSeleccionada = onRegionSeleccionada,
                etiqueta = stringResource(id = R.string.registro_campo_region)
            )

            if (uiState.errorRegion != null) {
                Text(
                    text = uiState.errorRegion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            SelectorDesplegable(
                opciones = comunasDisponibles,
                seleccionado = uiState.editComuna,
                onOpcionSeleccionada = onComunaSeleccionada,
                etiqueta = stringResource(id = R.string.registro_campo_comuna),
                habilitado = uiState.editRegion.isNotBlank()
            )

            if (uiState.errorComuna != null) {
                Text(
                    text = uiState.errorComuna,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = stringResource(id = R.string.registro_rubros_explicacion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ChipsRubros(
                rubrosSeleccionados = uiState.editRubros,
                onRubroToggled = onRubroToggled
            )

            if (uiState.errorRubros != null) {
                Text(
                    text = uiState.errorRubros,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            CampoTexto(
                valor = uiState.editInstagram,
                onValorCambiado = onInstagramCambiado,
                etiqueta = stringResource(id = R.string.registro_campo_instagram),
                textoAyuda = stringResource(id = R.string.registro_campo_instagram_ayuda),
                mensajeError = uiState.errorInstagram
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditarPerfilScreenPreview() {
    TextilCircularTheme {
        EditarPerfilContent(
            uiState = PerfilUiState(),
            onNombreCambiado = {},
            onNombreComercialCambiado = {},
            onRegionSeleccionada = {},
            onComunaSeleccionada = {},
            onRubroToggled = {},
            onInstagramCambiado = {},
            onGuardarCambios = {},
            onNavegarAtras = {}
        )
    }
}
