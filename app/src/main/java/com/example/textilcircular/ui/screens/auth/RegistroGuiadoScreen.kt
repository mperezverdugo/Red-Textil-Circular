package com.example.textilcircular.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.components.BarraProgresoPasos
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.components.ChipsRubros
import com.example.textilcircular.ui.components.SelectorDesplegable
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.util.Constantes
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Formulario de registro guiado en 4 pasos para nuevas emprendedoras conectado a RegistroGuiadoViewModel.
@Composable
fun RegistroGuiadoScreen(
    onNavegarAtras: () -> Unit,
    onRegistroCompletado: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistroGuiadoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistroGuiadoContent(
        uiState = uiState,
        onNombreCompletoCambiado = viewModel::onNombreCompletoCambiado,
        onRutCambiado = viewModel::onRutCambiado,
        onCorreoCambiado = viewModel::onCorreoCambiado,
        onPasswordCambiado = viewModel::onPasswordCambiado,
        onNombreComercialCambiado = viewModel::onNombreComercialCambiado,
        onRubroToggled = viewModel::onRubroToggled,
        onRegionSeleccionada = viewModel::onRegionSeleccionada,
        onComunaSeleccionada = viewModel::onComunaSeleccionada,
        onInstagramCambiado = viewModel::onInstagramCambiado,
        onAceptaLeyDatosCambiado = viewModel::onAceptaLeyDatosCambiado,
        onAvanzarPaso = { viewModel.avanzarPaso() },
        onRetrocederPaso = {
            if (!viewModel.retrocederPaso()) {
                onNavegarAtras()
            }
        },
        onFinalizarRegistro = {
            viewModel.finalizarRegistro(onExito = onRegistroCompletado)
        },
        onNavegarAtras = onNavegarAtras,
        modifier = modifier
    )
}

@Composable
fun RegistroGuiadoContent(
    uiState: RegistroGuiadoUiState,
    onNombreCompletoCambiado: (String) -> Unit,
    onRutCambiado: (String) -> Unit,
    onCorreoCambiado: (String) -> Unit,
    onPasswordCambiado: (String) -> Unit,
    onNombreComercialCambiado: (String) -> Unit,
    onRubroToggled: (Rubro) -> Unit,
    onRegionSeleccionada: (String) -> Unit,
    onComunaSeleccionada: (String) -> Unit,
    onInstagramCambiado: (String) -> Unit,
    onAceptaLeyDatosCambiado: (Boolean) -> Unit,
    onAvanzarPaso: () -> Unit,
    onRetrocederPaso: () -> Unit,
    onFinalizarRegistro: () -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comunasDisponibles = remember(uiState.regionSeleccionada) {
        if (uiState.regionSeleccionada.isNotBlank()) {
            DatosDemo.RegionesConComunas[uiState.regionSeleccionada] ?: emptyList()
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.registro_titulo),
                onNavegarAtras = onRetrocederPaso
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.pasoActual < Constantes.TOTAL_PASOS_REGISTRO) {
                    BotonPrincipal(
                        texto = stringResource(id = R.string.accion_siguiente),
                        onClick = onAvanzarPaso
                    )
                } else {
                    BotonPrincipal(
                        texto = stringResource(id = R.string.registro_boton_finalizar),
                        onClick = onFinalizarRegistro,
                        habilitado = !uiState.estaCargando && uiState.aceptaLeyDatos
                    )
                }

                if (uiState.pasoActual > 1) {
                    BotonSecundario(
                        texto = stringResource(id = R.string.accion_atras),
                        onClick = onRetrocederPaso
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val tituloPasoActual = when (uiState.pasoActual) {
                1 -> stringResource(id = R.string.registro_paso1_titulo)
                2 -> stringResource(id = R.string.registro_paso2_titulo)
                3 -> stringResource(id = R.string.registro_paso3_titulo)
                else -> stringResource(id = R.string.registro_paso4_titulo)
            }

            BarraProgresoPasos(
                pasoActual = uiState.pasoActual,
                totalPasos = Constantes.TOTAL_PASOS_REGISTRO,
                tituloPaso = tituloPasoActual
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (uiState.pasoActual) {
                // Paso 1: Datos personales
                1 -> {
                    CampoTexto(
                        valor = uiState.nombreCompleto,
                        onValorCambiado = onNombreCompletoCambiado,
                        etiqueta = stringResource(id = R.string.registro_campo_nombre),
                        textoAyuda = stringResource(id = R.string.registro_campo_nombre_ayuda),
                        mensajeError = uiState.errorNombre
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = uiState.rut,
                        onValorCambiado = onRutCambiado,
                        etiqueta = stringResource(id = R.string.registro_campo_rut),
                        textoAyuda = stringResource(id = R.string.registro_campo_rut_ayuda),
                        mensajeError = uiState.errorRut
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambiado = onCorreoCambiado,
                        etiqueta = stringResource(id = R.string.registro_campo_correo),
                        textoAyuda = stringResource(id = R.string.registro_campo_correo_ayuda),
                        mensajeError = uiState.errorCorreo,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = uiState.password,
                        onValorCambiado = onPasswordCambiado,
                        etiqueta = stringResource(id = R.string.registro_campo_password),
                        textoAyuda = stringResource(id = R.string.registro_campo_password_ayuda),
                        mensajeError = uiState.errorPassword,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                // Paso 2: Emprendimiento y rubros
                2 -> {
                    CampoTexto(
                        valor = uiState.nombreComercial,
                        onValorCambiado = onNombreComercialCambiado,
                        etiqueta = stringResource(id = R.string.registro_campo_nombre_comercial),
                        textoAyuda = stringResource(id = R.string.registro_campo_nombre_comercial_ayuda),
                        mensajeError = uiState.errorNombreComercial
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.registro_rubros_explicacion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ChipsRubros(
                        rubrosSeleccionados = uiState.rubrosSeleccionados,
                        onRubroToggled = onRubroToggled
                    )

                    if (uiState.errorRubros != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorRubros,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Paso 3: Ubicación y contacto
                3 -> {
                    SelectorDesplegable(
                        opciones = DatosDemo.ListaRegiones,
                        seleccionado = uiState.regionSeleccionada,
                        onOpcionSeleccionada = onRegionSeleccionada,
                        etiqueta = stringResource(id = R.string.registro_campo_region)
                    )

                    if (uiState.errorRegion != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.errorRegion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SelectorDesplegable(
                        opciones = comunasDisponibles,
                        seleccionado = uiState.comunaSeleccionada,
                        onOpcionSeleccionada = onComunaSeleccionada,
                        etiqueta = stringResource(id = R.string.registro_campo_comuna),
                        habilitado = uiState.regionSeleccionada.isNotBlank()
                    )

                    if (uiState.errorComuna != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.errorComuna,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = uiState.instagram,
                        onValorCambiado = onInstagramCambiado,
                        etiqueta = stringResource(id = R.string.registro_campo_instagram),
                        textoAyuda = stringResource(id = R.string.registro_campo_instagram_ayuda),
                        mensajeError = uiState.errorInstagram
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.registro_aviso_chat),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Paso 4: Confirmación y condiciones
                4 -> {
                    Text(
                        text = stringResource(id = R.string.registro_resumen_explicacion),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Titular: " + uiState.nombreCompleto.ifBlank { "Francisca Silva" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "RUT: " + uiState.rut.ifBlank { "12.345.678-9" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Correo: " + uiState.correo.ifBlank { "francisca@ejemplo.cl" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            HorizontalDivider()
                            Text(
                                text = "Emprendimiento: " + uiState.nombreComercial.ifBlank { "Taller Hilos del Sur" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Rubros: " + if (uiState.rubrosSeleccionados.isEmpty()) "Sin rubros" else uiState.rubrosSeleccionados.joinToString { it.nombre },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Ubicación: " + (if (uiState.regionSeleccionada.isBlank()) "Sin región" else uiState.regionSeleccionada) + " - " + (if (uiState.comunaSeleccionada.isBlank()) "Sin comuna" else uiState.comunaSeleccionada),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (uiState.instagram.isNotBlank()) {
                                Text(
                                    text = "Instagram: @" + uiState.instagram,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = uiState.aceptaLeyDatos,
                            onCheckedChange = onAceptaLeyDatosCambiado,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.registro_ley_datos),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroGuiadoScreenPreview() {
    TextilCircularTheme {
        RegistroGuiadoContent(
            uiState = RegistroGuiadoUiState(),
            onNombreCompletoCambiado = {},
            onRutCambiado = {},
            onCorreoCambiado = {},
            onPasswordCambiado = {},
            onNombreComercialCambiado = {},
            onRubroToggled = {},
            onRegionSeleccionada = {},
            onComunaSeleccionada = {},
            onInstagramCambiado = {},
            onAceptaLeyDatosCambiado = {},
            onAvanzarPaso = {},
            onRetrocederPaso = {},
            onFinalizarRegistro = {},
            onNavegarAtras = {}
        )
    }
}
