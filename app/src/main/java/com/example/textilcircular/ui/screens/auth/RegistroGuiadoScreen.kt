package com.example.textilcircular.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

/**
 * Pantalla 3: Registro Guiado de 4 Pasos (RF-01 a RF-07)
 * Diseñada para máxima simplicidad, accesibilidad y acompañamiento a la usuaria.
 */
@Composable
fun RegistroGuiadoScreen(
    onNavegarAtras: () -> Unit,
    onRegistroCompletado: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pasoActual by remember { mutableIntStateOf(1) }

    // Paso 1: Datos Personales
    var nombreCompleto by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Paso 2: Emprendimiento
    var nombreComercial by remember { mutableStateOf("") }
    val rubrosSeleccionados = remember { mutableStateListOf<Rubro>() }

    // Paso 3: Ubicación y Contacto
    var regionSeleccionada by remember { mutableStateOf("") }
    var comunaSeleccionada by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }

    // Paso 4: Confirmación y Ley de Datos
    var aceptaLeyDatos by remember { mutableStateOf(false) }

    // Control de demostración para visualización de errores visuales (RNF-04, RF-05, RF-06)
    var simularErroresDemo by remember { mutableStateOf(false) }

    val comunasDisponibles = remember(regionSeleccionada) {
        if (regionSeleccionada.isNotBlank()) {
            DatosDemo.RegionesConComunas[regionSeleccionada] ?: emptyList()
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.registro_titulo),
                onNavegarAtras = {
                    if (pasoActual > 1) {
                        pasoActual--
                    } else {
                        onNavegarAtras()
                    }
                }
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
                if (pasoActual < Constantes.TOTAL_PASOS_REGISTRO) {
                    BotonPrincipal(
                        texto = stringResource(id = R.string.accion_siguiente),
                        onClick = { pasoActual++ }
                    )
                } else {
                    // TODO(Capa 2): Conectar con AuthViewModel para invocar registro en el backend
                    BotonPrincipal(
                        texto = stringResource(id = R.string.registro_boton_finalizar),
                        onClick = onRegistroCompletado,
                        habilitado = aceptaLeyDatos
                    )
                }

                if (pasoActual > 1) {
                    BotonSecundario(
                        texto = stringResource(id = R.string.accion_atras),
                        onClick = { pasoActual-- }
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

            // Barra indicadora de avance
            val tituloPasoActual = when (pasoActual) {
                1 -> stringResource(id = R.string.registro_paso1_titulo)
                2 -> stringResource(id = R.string.registro_paso2_titulo)
                3 -> stringResource(id = R.string.registro_paso3_titulo)
                else -> stringResource(id = R.string.registro_paso4_titulo)
            }

            BarraProgresoPasos(
                pasoActual = pasoActual,
                totalPasos = Constantes.TOTAL_PASOS_REGISTRO,
                tituloPaso = tituloPasoActual
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Switch demostrativo de errores
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Simular errores visuales",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = simularErroresDemo,
                    onCheckedChange = { simularErroresDemo = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (pasoActual) {
                // ==========================================
                // PASO 1: DATOS PERSONALES
                // ==========================================
                1 -> {
                    CampoTexto(
                        valor = nombreCompleto,
                        onValorCambiado = { nombreCompleto = it },
                        etiqueta = stringResource(id = R.string.registro_campo_nombre),
                        textoAyuda = stringResource(id = R.string.registro_campo_nombre_ayuda),
                        mensajeError = if (simularErroresDemo) stringResource(id = R.string.error_formato_nombre) else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = rut,
                        onValorCambiado = { rut = it },
                        etiqueta = stringResource(id = R.string.registro_campo_rut),
                        textoAyuda = stringResource(id = R.string.registro_campo_rut_ayuda),
                        mensajeError = if (simularErroresDemo) stringResource(id = R.string.error_rut_duplicado) else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = correo,
                        onValorCambiado = { correo = it },
                        etiqueta = stringResource(id = R.string.registro_campo_correo),
                        textoAyuda = stringResource(id = R.string.registro_campo_correo_ayuda),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = password,
                        onValorCambiado = { password = it },
                        etiqueta = stringResource(id = R.string.registro_campo_password),
                        textoAyuda = stringResource(id = R.string.registro_campo_password_ayuda),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                // ==========================================
                // PASO 2: EMPRENDIMIENTO Y RUBROS
                // ==========================================
                2 -> {
                    CampoTexto(
                        valor = nombreComercial,
                        onValorCambiado = { nombreComercial = it },
                        etiqueta = stringResource(id = R.string.registro_campo_nombre_comercial),
                        textoAyuda = stringResource(id = R.string.registro_campo_nombre_comercial_ayuda)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.registro_rubros_explicacion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ChipsRubros(
                        rubrosSeleccionados = rubrosSeleccionados,
                        onRubroToggled = { rubro ->
                            if (rubrosSeleccionados.contains(rubro)) {
                                rubrosSeleccionados.remove(rubro)
                            } else {
                                rubrosSeleccionados.add(rubro)
                            }
                        }
                    )
                }

                // ==========================================
                // PASO 3: UBICACIÓN Y CONTACTO
                // ==========================================
                3 -> {
                    // TODO(Capa 3): La lista completa de comunas vendrá del dataset oficial de la Fundación
                    SelectorDesplegable(
                        opciones = DatosDemo.ListaRegiones,
                        seleccionado = regionSeleccionada,
                        onOpcionSeleccionada = { nuevaRegion ->
                            regionSeleccionada = nuevaRegion
                            // RF-03: Al cambiar de región se limpia la comuna anterior
                            comunaSeleccionada = ""
                        },
                        etiqueta = stringResource(id = R.string.registro_campo_region)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SelectorDesplegable(
                        opciones = comunasDisponibles,
                        seleccionado = comunaSeleccionada,
                        onOpcionSeleccionada = { comunaSeleccionada = it },
                        etiqueta = stringResource(id = R.string.registro_campo_comuna),
                        habilitado = regionSeleccionada.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampoTexto(
                        valor = instagram,
                        onValorCambiado = { instagram = it },
                        etiqueta = stringResource(id = R.string.registro_campo_instagram),
                        textoAyuda = stringResource(id = R.string.registro_campo_instagram_ayuda),
                        mensajeError = if (simularErroresDemo) stringResource(id = R.string.error_formato_instagram) else null
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

                // ==========================================
                // PASO 4: CONFIRMACIÓN Y LEY DE DATOS
                // ==========================================
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
                                text = "Titular: " + nombreCompleto.ifBlank { "Francisca Silva" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "RUT: " + rut.ifBlank { "12.345.678-9" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Correo: " + correo.ifBlank { "francisca@ejemplo.cl" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            HorizontalDivider()
                            Text(
                                text = "Emprendimiento: " + nombreComercial.ifBlank { "Taller Hilos del Sur" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Rubros: " + if (rubrosSeleccionados.isEmpty()) "Suprarreciclaje" else rubrosSeleccionados.joinToString { it.nombre },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Ubicación: " + (if (regionSeleccionada.isBlank()) "Valparaíso" else regionSeleccionada) + " - " + (if (comunaSeleccionada.isBlank()) "Valparaíso" else comunaSeleccionada),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = aceptaLeyDatos,
                            onCheckedChange = { aceptaLeyDatos = it },
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

                    if (simularErroresDemo && !aceptaLeyDatos) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.error_debe_aceptar_ley),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroGuiadoScreenPreview() {
    TextilCircularTheme {
        RegistroGuiadoScreen(
            onNavegarAtras = {},
            onRegistroCompletado = {}
        )
    }
}
